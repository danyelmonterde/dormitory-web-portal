#!/bin/bash
# create-aws-infra.sh: Automated script to set up AWS resources in ap-southeast-1
set -e

REGION="ap-southeast-1"
KEY_NAME="my-ssh-key"
AMI_ID="ami-0f79166d4c42e6c1e" # Latest AL2023 x86_64
INSTANCE_TYPE="t3.large"       # 2 vCPUs, 8 GB RAM

echo "=== 1. Checking default VPC ==="
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query "Vpcs[0].VpcId" --region $REGION --output text)
echo "Default VPC ID: $VPC_ID"

echo "=== 2. Creating Security Group ==="
SG_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=dorm-portal-sg" "Name=vpc-id,Values=$VPC_ID" --query "SecurityGroups[0].GroupId" --region $REGION --output text 2>/dev/null || true)

if [ "$SG_ID" == "None" ] || [ -z "$SG_ID" ]; then
    echo "Creating security group 'dorm-portal-sg'..."
    SG_ID=$(aws ec2 create-security-group --group-name "dorm-portal-sg" --description "Dorm Portal Web security group" --vpc-id "$VPC_ID" --region $REGION --query "GroupId" --output text)
    echo "Created SG ID: $SG_ID"
    
    echo "Authorizing inbound rules (22 for SSH, 80 for HTTP)..."
    aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 22 --cidr 0.0.0.0/0 --region $REGION
    aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $REGION
else
    echo "Security group 'dorm-portal-sg' already exists with ID: $SG_ID"
fi

echo "=== 3. Creating IAM Role and Instance Profile ==="
cat <<EOF > trust-policy.json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

cat <<EOF > secrets-policy.json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "secretsmanager:GetSecretValue",
      "Resource": "arn:aws:secretsmanager:${REGION}:*:secret:*-dorm-secrets*"
    }
  ]
}
EOF

ROLE_EXISTS=$(aws iam get-role --role-name "dorm-portal-ec2-role" 2>/dev/null || true)
if [ -z "$ROLE_EXISTS" ]; then
    echo "Creating IAM Role 'dorm-portal-ec2-role'..."
    aws iam create-role --role-name "dorm-portal-ec2-role" --assume-role-policy-document file://trust-policy.json
    aws iam put-role-policy --role-name "dorm-portal-ec2-role" --policy-name "DormPortalSecretsPolicy" --policy-document file://secrets-policy.json
else
    echo "IAM Role 'dorm-portal-ec2-role' already exists."
fi

PROFILE_EXISTS=$(aws iam get-instance-profile --instance-profile-name "dorm-portal-instance-profile" 2>/dev/null || true)
if [ -z "$PROFILE_EXISTS" ]; then
    echo "Creating IAM Instance Profile 'dorm-portal-instance-profile'..."
    aws iam create-instance-profile --instance-profile-name "dorm-portal-instance-profile"
    aws iam add-role-to-instance-profile --instance-profile-name "dorm-portal-instance-profile" --role-name "dorm-portal-ec2-role"
    echo "Waiting 10 seconds for IAM roles to propagate..."
    sleep 10
else
    echo "IAM Instance Profile 'dorm-portal-instance-profile' already exists."
fi

rm -f trust-policy.json secrets-policy.json

echo "=== 4. Creating Secrets in Secrets Manager ==="
# Helper to create secret if it does not exist
create_secret_if_missing() {
    local secret_name=$1
    local exists=$(aws secretsmanager describe-secret --secret-id "$secret_name" --region $REGION 2>/dev/null || true)
    if [ -z "$exists" ]; then
        echo "Creating secret: $secret_name"
        # Seed with placeholder data based on .env
        local secret_string='{"DB_URL":"jdbc:mysql://localhost:3306/dormitory_local","DB_USER":"root","DB_PASS":"password","PAYMONGO_SECRET_KEY":"sk_test_1234567890abcdef12345678","JWT_SECRET":"dGhpcy1pcy1hLXNlY3VyZS1iYXNlNjQtZW5jb2RlZC1qd3Qtc2VjcmV0LWtleS1hdC1sZWFzdC0yNTYtYml0cw=="}'
        aws secretsmanager create-secret --name "$secret_name" --secret-string "$secret_string" --region $REGION
    else
        echo "Secret '$secret_name' already exists."
    fi
}

create_secret_if_missing "dev-dorm-secrets"
create_secret_if_missing "uat-dorm-secrets"
create_secret_if_missing "prod-dorm-secrets"

echo "=== 5. Writing User Data Script ==="
cat <<EOF > user-data.sh
#!/bin/bash
sudo dnf update -y
sudo dnf install -y docker bzip2
sudo systemctl enable docker --now
sudo usermod -aG docker ec2-user
EOF

echo "=== 6. Launching EC2 Instances ==="
launch_instance() {
    local name=$1
    local env_name=$2
    
    # Check if instance already exists to prevent duplication
    local existing_id=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=$name" "Name=instance-state-name,Values=running,pending" --query "Reservations[0].Instances[0].InstanceId" --region $REGION --output text 2>/dev/null || true)
    
    if [ "$existing_id" != "None" ] && [ -n "$existing_id" ]; then
        echo "Active instance '$name' already exists with ID: $existing_id"
    else
        echo "Launching instance '$name'..."
        local inst_id=$(aws ec2 run-instances \
            --image-id "$AMI_ID" \
            --instance-type "$INSTANCE_TYPE" \
            --key-name "$KEY_NAME" \
            --security-group-ids "$SG_ID" \
            --iam-instance-profile Name="dorm-portal-instance-profile" \
            --user-data file://user-data.sh \
            --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$name},{Key=Project,Value=DormPortal},{Key=Environment,Value=$env_name}]" \
            --region $REGION \
            --query "Instances[0].InstanceId" \
            --output text)
        echo "Launched Instance ID: $inst_id"
    fi
}

launch_instance "dorm-portal-dev" "dev"
launch_instance "dorm-portal-uat" "uat"
launch_instance "dorm-portal-prod" "prod"

rm -f user-data.sh

echo "=== 7. Waiting for Instances to get Public IPs ==="
echo "Giving instances 15 seconds to boot and allocate IPs..."
sleep 15

get_ip() {
    local name=$1
    aws ec2 describe-instances --filters "Name=tag:Name,Values=$name" "Name=instance-state-name,Values=running,pending" --query "Reservations[0].Instances[0].PublicIpAddress" --region $REGION --output text
}

DEV_IP=$(get_ip "dorm-portal-dev")
UAT_IP=$(get_ip "dorm-portal-uat")
PROD_IP=$(get_ip "dorm-portal-prod")

echo "=== 8. AWS Infrastructure Setup Completed ==="
echo "=========================================="
echo "DormPortal Dev IP:   $DEV_IP"
echo "DormPortal UAT IP:   $UAT_IP"
echo "DormPortal Prod IP:  $PROD_IP"
echo "=========================================="
echo "Ensure your Jenkins pipeline credentials matches: ec2-dev-ip, ec2-uat-ip, ec2-prod-ip."

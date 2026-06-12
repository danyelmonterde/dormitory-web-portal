package com.example.dormportal.config;

import com.example.dormportal.model.Facility;
import com.example.dormportal.model.Role;
import com.example.dormportal.model.Room;
import com.example.dormportal.model.User;
import com.example.dormportal.repository.FacilityRepository;
import com.example.dormportal.repository.RoomRepository;
import com.example.dormportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FacilityRepository facilityRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, RoomRepository roomRepository,
                          FacilityRepository facilityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.facilityRepository = facilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Rooms
        if (roomRepository.count() == 0) {
            Room r101 = new Room(null, "Room 101", 4, 8500.0, "DormWifi101@Secure");
            Room r102 = new Room(null, "Room 102", 2, 12000.0, "DormWifi102@Super");
            Room r103 = new Room(null, "Room 103", 4, 7500.0, "DormWifi103@Fast");
            roomRepository.save(r101);
            roomRepository.save(r102);
            roomRepository.save(r103);
            System.out.println("Rooms seeded successfully.");
        }

        // 2. Seed Users
        if (userRepository.count() == 0) {
            // Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("adminpass"));
            admin.setEmail("admin@dormportal.com");
            admin.setFullName("System Admin");
            admin.setPhoneNumber("+639000000000");
            admin.setRole(Role.ROLE_ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);

            // Dormer 1 (assigned to Room 101)
            Room r101 = roomRepository.findByRoomNumber("Room 101").orElse(null);
            User dormer1 = new User();
            dormer1.setUsername("dormer1");
            dormer1.setPassword(passwordEncoder.encode("dormerpass"));
            dormer1.setEmail("john.doe@gmail.com");
            dormer1.setFullName("John Doe");
            dormer1.setPhoneNumber("+639123456789");
            dormer1.setRole(Role.ROLE_DORMER);
            dormer1.setEnabled(true);
            dormer1.setRoom(r101);
            userRepository.save(dormer1);

            // Dormer 2 (assigned to Room 101)
            User dormer2 = new User();
            dormer2.setUsername("dormer2");
            dormer2.setPassword(passwordEncoder.encode("dormerpass"));
            dormer2.setEmail("jane.smith@gmail.com");
            dormer2.setFullName("Jane Smith");
            dormer2.setPhoneNumber("+639177654321");
            dormer2.setRole(Role.ROLE_DORMER);
            dormer2.setEnabled(true);
            dormer2.setRoom(r101);
            userRepository.save(dormer2);

            // Dormer 3 (assigned to Room 102)
            Room r102 = roomRepository.findByRoomNumber("Room 102").orElse(null);
            User dormer3 = new User();
            dormer3.setUsername("dormer3");
            dormer3.setPassword(passwordEncoder.encode("dormerpass"));
            dormer3.setEmail("robert.lee@gmail.com");
            dormer3.setFullName("Robert Lee");
            dormer3.setPhoneNumber("+639198765432");
            dormer3.setRole(Role.ROLE_DORMER);
            dormer3.setEnabled(true);
            dormer3.setRoom(r102);
            userRepository.save(dormer3);

            System.out.println("Default users seeded successfully.");
        }

        // 3. Seed Facilities
        if (facilityRepository.count() == 0) {
            facilityRepository.save(new Facility(null, "Lobby Air Conditioner", "OPERATIONAL", "Main lobby cooling system"));
            facilityRepository.save(new Facility(null, "Washing Machine #1", "OPERATIONAL", "Laundry area, ground floor"));
            facilityRepository.save(new Facility(null, "Laundry Dryer #1", "UNDER_REPAIR", "Heating element issue, scheduled for repair"));
            facilityRepository.save(new Facility(null, "Kitchen Microwave", "OPERATIONAL", "Second floor common pantry"));
            facilityRepository.save(new Facility(null, "Study Hall WiFi Router", "OPERATIONAL", "5GHz enabled study hall access point"));
            System.out.println("Facilities seeded successfully.");
        }
    }
}

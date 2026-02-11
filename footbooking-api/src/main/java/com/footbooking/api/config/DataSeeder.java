package com.footbooking.api.config;

import com.footbooking.api.auth.model.Role;
import com.footbooking.api.auth.repository.RoleRepository;
import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

//@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final TerrainRepository terrainRepository;
        private final BookingJdbcRepository bookingJdbcRepository;
        private final PasswordEncoder passwordEncoder;
        private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
        private final com.footbooking.api.payment.repository.BankAccountRepository bankAccountRepository;

        private final Random random = new Random();

        @Override
        public void run(String... args) throws Exception {
                log.info("Starting Data Seeding...");
                seedRoles();
                seedUsers();
                seedTerrains();
                seedBankAccounts();
                // seedBookings(); // Disabled as per user request
                ensureAssignments();
                log.info("Data Seeding Completed.");
        }

        private void seedRoles() {
                if (roleRepository.count() == 0) {
                        roleRepository.save(Role.builder().name("USER").build());
                        roleRepository.save(Role.builder().name("ADMIN").build());
                        roleRepository.save(Role.builder().name("SUPERADMIN").build());
                        log.info("Seeded Roles: USER, ADMIN, SUPERADMIN");
                }
        }

        private void seedUsers() {
                // Clean up bad data
                List<User> badUsers = userRepository.findAll().stream()
                                .filter(u -> u.getName() == null)
                                .toList();
                if (!badUsers.isEmpty()) {
                        log.info("Deleting {} users with null names...", badUsers.size());
                        userRepository.deleteAll(badUsers);
                }

                Set<Role> userRole = new HashSet<>();
                roleRepository.findByName("USER").ifPresent(userRole::add);

                // Ensure test user exists
                if (!userRepository.existsByEmail("test@fb.com")) {
                        userRepository.save(createUser("Test User", "test@fb.com", passwordEncoder.encode("123456"),
                                        userRole));
                        log.info("Seeded test user: test@fb.com");
                }

                // Ensure admin user exists
                if (!userRepository.existsByEmail("admin@fb.com")) {
                        Set<Role> adminRoles = new HashSet<>(userRole);
                        roleRepository.findByName("ADMIN").ifPresent(adminRoles::add);
                        userRepository.save(createUser("Admin User", "admin@fb.com", passwordEncoder.encode("123456"),
                                        adminRoles));
                        log.info("Seeded admin user: admin@fb.com");
                }

                // Ensure superadmin user exists
                if (!userRepository.existsByEmail("superadmin@fb.com")) {
                        Set<Role> superRoles = new HashSet<>(userRole);
                        roleRepository.findByName("ADMIN").ifPresent(superRoles::add);
                        roleRepository.findByName("SUPERADMIN").ifPresent(superRoles::add);
                        userRepository.save(createUser("Super Admin", "superadmin@fb.com",
                                        passwordEncoder.encode("123456"), superRoles));
                        log.info("Seeded superadmin user: superadmin@fb.com");
                }

                // Remove extra users (cleanup)
                List<User> allUsers = userRepository.findAll();
                List<User> usersToDelete = new ArrayList<>();
                List<User> usersToWipeBookings = new ArrayList<>();

                Set<String> keptEmails = Set.of("admin@fb.com", "superadmin@fb.com", "test@fb.com");
                String keptName = "Joueur 42302133";

                for (User user : allUsers) {
                        boolean isAdminOrSuper = user.getRoles().stream()
                                        .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("SUPERADMIN"));

                        boolean isKeptUser = keptEmails.contains(user.getEmail()) || keptName.equals(user.getName());

                        if (!isAdminOrSuper && !isKeptUser) {
                                usersToDelete.add(user);
                        } else if (keptName.equals(user.getName())) {
                                // For Joueur 42302133, assuming we want to clean their old random bookings
                                // but keep the user. We can delete all their bookings and let them start
                                // fresh or keep only recent ones.
                                // Given the user request that counts are wrong (12 instead of 1), let's clear
                                // them.
                                // Actually, if we clear them, the legitimate one might be lost too.
                                // But since the user says "il a fait une seule réservation", it implies
                                // they just did it. If we wipe, they lose it.
                                // However, without complex logic to identify "the real one", wiping is safer
                                // to match "fresh start" concept, or we just leave it and tell them to reset.
                                // The explicit request was "Corriger ceci".
                                // I will add them to a list to wipe bookings for, but NOT delete the user.
                                usersToWipeBookings.add(user);
                        }
                }
        }

        private User createUser(String name, String email, String password, Set<Role> roles) {
                // Generate a random avatar URL (using a placeholder service)
                String avatar = "https://i.pravatar.cc/150?u=" + email;

                return User.builder()
                                .name(name)
                                .email(email)
                                .password(password)
                                .avatar(avatar)
                                .score(random.nextInt(1000)) // Random score 0-999
                                .createdAt(LocalDateTime.now())
                                .enabled(true)
                                .roles(roles)
                                .build();
        }

        private void seedTerrains() {
                if (terrainRepository.count() > 0) {
                        log.info("Terrains already seeded.");
                        return;
                }

                List<Terrain> terrains = List.of(
                                createTerrain("Five Paris 17", "Paris", 80.0),
                                createTerrain("UrbanSoccer La Defense", "Paris", 90.0),
                                createTerrain("Le Five Villette", "Paris", 85.0),
                                createTerrain("Z5 Bois Senart", "Melun", 70.0),
                                createTerrain("Soccer Park Strasbourg", "Strasbourg", 75.0),
                                createTerrain("UrbanSoccer Lyon Parilly", "Lyon", 80.0),
                                createTerrain("FootSal Lille", "Lille", 60.0),
                                createTerrain("Five Bordeaux", "Bordeaux", 70.0),
                                createTerrain("Arena 18", "Marseille", 80.0),
                                createTerrain("Complexe Sportif Toulouse", "Toulouse", 65.0));

                terrainRepository.saveAll(terrains);
                log.info("Seeded Terrains.");
        }

        private Terrain createTerrain(String name, String city, Double price) {
                return Terrain.builder()
                                .name(name)
                                .city(city)
                                .pricePerHour(BigDecimal.valueOf(price))
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        private void seedBankAccounts() {
                if (bankAccountRepository.count() > 0) {
                        log.info("Bank accounts already seeded.");
                        return;
                }

                List<Terrain> terrains = terrainRepository.findAll();
                if (terrains.isEmpty()) {
                        log.info("No terrains found, skipping bank account seeding.");
                        return;
                }

                // French bank names
                String[] bankNames = {
                                "Banquili", "Crédit Agricole", "Société Générale", "Banque Populaire",
                                "Caisse d'Épargne", "LCL", "Crédit Mutuel", "La Banque Postale",
                                "CIC", "Boursorama Banque"
                };


                int accountsCreated = 0;
                for (int i = 0; i < terrains.size(); i++) {
                        Terrain terrain = terrains.get(i);

                        // Generate realistic French bank account details
                        String bankName = bankNames[i % bankNames.length];

                        // Generate French account number (11 digits)
                        String accountNumber = String.format("%011d", 10000000000L + random.nextInt(90000000));

                        // Generate French RIB (27 characters: FR + 2 check digits + 23 digits)
                        String bankCode = String.format("%05d", 10000 + random.nextInt(90000));
                        String branchCode = String.format("%05d", 10000 + random.nextInt(90000));
                        String accountNum = String.format("%011d", 10000000000L + random.nextInt(90000000));
                        String key = String.format("%02d", random.nextInt(97));

                        // Additional info variations
                        String[] additionalInfos = {
                                        "Paiement par virement uniquement",
                                        "Virement bancaire ou espèces acceptés",
                                        "Merci d'indiquer le numéro de réservation dans le libellé",
                                        "Paiement à effectuer 24h avant la réservation",
                                        "Virement SEPA uniquement",
                                        null // Some terrains without additional info
                        };

                        com.footbooking.api.payment.model.BankAccount bankAccount = new com.footbooking.api.payment.model.BankAccount();
                        bankAccount.setTerrainId(terrain.getId());
                        bankAccount.setBankName(bankName);
                        bankAccount.setAccountNumber(accountNumber);
                        bankAccount.setAdditionalInfo(additionalInfos[i % additionalInfos.length]);

                        bankAccountRepository.save(bankAccount);
                        accountsCreated++;
                }

                log.info("Seeded {} bank accounts for terrains.", accountsCreated);
        }

        private void seedBookings() {
                // Since we don't have a count method easily on JdbcRepository, we'll try to
                // insert anyway
                // Or we can rely on catching exceptions or just assuming it's fine if terrains
                // were empty.
                // To be safe, let's just create some bookings if valid users and terrains
                // exist.

                List<User> users = userRepository.findAll();
                List<Terrain> terrains = terrainRepository.findAll();

                if (users.isEmpty() || terrains.isEmpty()) {
                        return;
                }

                // We will seed 100 random bookings
                // Note: this might fail if we violate unique constraints
                // (user+terrain+date+hour? or just id?)
                // BookingJdbcRepository usually just inserts.
                // We need to implement a check to avoid duplication if we re-run?
                // For now, let's assume we run this once or if data is missing.

                int bookingsToCreate = 100;
                int created = 0;

                for (int i = 0; i < bookingsToCreate; i++) {
                        User user = users.get(random.nextInt(users.size()));
                        Terrain terrain = terrains.get(random.nextInt(terrains.size()));
                        LocalDate date = LocalDate.now().plusDays(random.nextInt(14)); // next 2 weeks
                        int hour = 10 + random.nextInt(12); // 10h to 22h

                        // Check availability basic check (optional but good)
                        List<Integer> bookedHours = bookingJdbcRepository.findBookedHours(terrain.getId(), date);
                        if (!bookedHours.contains(hour)) {
                                try {
                                        bookingJdbcRepository.createBooking(user.getId(), terrain.getId(), date, hour, null
                                                        );
                                        created++;
                                } catch (Exception e) {
                                        // Ignore conflicts
                                }
                        }
                }
                log.info("Seeded {} bookings.", created);
        }

        private void ensureAssignments() {
                userRepository.findByEmail("admin@fb.com").ifPresent(admin -> {
                        userRepository.findByEmail("superadmin@fb.com").ifPresent(superAdmin -> {
                                List<Terrain> terrains = terrainRepository.findAll();
                                boolean updated = false;
                                for (Terrain terrain : terrains) {
                                        // Assign "Five Paris 17" to admin, others to superadmin
                                        if ("Five Paris 17".equalsIgnoreCase(terrain.getName())) {
                                                if (terrain.getOwner() == null || !terrain.getOwner().getEmail()
                                                                .equals(admin.getEmail())) {
                                                        terrain.setOwner(admin);
                                                        terrainRepository.save(terrain);
                                                        updated = true;
                                                }
                                        } else {
                                                // All others to superadmin
                                                if (terrain.getOwner() == null || terrain.getOwner().getEmail()
                                                                .equals(admin.getEmail())) {
                                                        terrain.setOwner(superAdmin);
                                                        terrainRepository.save(terrain);
                                                        updated = true;
                                                }
                                        }
                                }
                                if (updated) {
                                        log.info("Re-assigned terrains: 'Five Paris 17' to Admin, others to SuperAdmin.");
                                }
                        });
                });
        }
}

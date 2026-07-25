package com.recoverease;

import com.recoverease.entity.ResourceItem;
import com.recoverease.entity.User;
import com.recoverease.repository.ResourceRepository;
import com.recoverease.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class RecoverEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecoverEaseApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(UserRepository userRepo,
                               ResourceRepository resourceRepo,
                               PasswordEncoder encoder) {
        return args -> {
            // Seed demo users
            if (userRepo.count() == 0) {
                User user1 = new User();
                user1.setName("Alex Johnson");
                user1.setEmail("testuser@recoverease.com");
                user1.setPasswordHash(encoder.encode("Demo@123"));
                user1.setRole("INDIVIDUAL");
                user1.setPreferredLanguage("en");
                userRepo.save(user1);

                User user2 = new User();
                user2.setName("Sarah Williams");
                user2.setEmail("caregiver@recoverease.com");
                user2.setPasswordHash(encoder.encode("Demo@123"));
                user2.setRole("CAREGIVER");
                user2.setPreferredLanguage("en");
                userRepo.save(user2);
            }

            // Seed resources
            if (resourceRepo.count() == 0) {
                List<ResourceItem> resources = List.of(
                    new ResourceItem(null, "Understanding Relapse", "RELAPSE",
                        "SAMHSA", "https://www.samhsa.gov",
                        "Relapse is a common part of recovery. It does not mean failure. Recognizing warning signs early — such as increased cravings, isolation, or reconnecting with past substance-using friends — can help prevent a full relapse."),
                    new ResourceItem(null, "Coping Strategies for Cravings", "COPING",
                        "NIDA", "https://www.drugabuse.gov",
                        "Effective coping strategies include mindfulness breathing, physical exercise, reaching out to a sponsor or support person, engaging in hobbies, and using distraction techniques until the craving passes."),
                    new ResourceItem(null, "Overdose Recognition and Response", "OVERDOSE",
                        "CDC", "https://www.cdc.gov",
                        "Signs of opioid overdose include slow or stopped breathing, unresponsiveness, blue-tinged lips. If suspected, call emergency services immediately and administer naloxone if available."),
                    new ResourceItem(null, "Supporting a Loved One in Recovery", "FAMILY",
                        "SAMHSA", "https://www.samhsa.gov",
                        "Families play a critical role. Use compassionate, non-judgmental communication. Avoid enabling behaviors. Seek family counseling and join support groups like Al-Anon or Nar-Anon."),
                    new ResourceItem(null, "Building a Recovery Support Network", "RECOVERY",
                        "NIDA", "https://www.drugabuse.gov",
                        "A strong support network includes a sponsor, therapist, trusted family members, and peers in recovery. Regular group meetings like AA or NA provide community, accountability, and hope."),
                    new ResourceItem(null, "Mindfulness for Recovery", "COPING",
                        "WHO", "https://www.who.int",
                        "Mindfulness-based relapse prevention (MBRP) has strong evidence. It teaches individuals to observe cravings without acting on them. Just 5-10 minutes of daily practice can reduce relapse risk."),
                    new ResourceItem(null, "Harm Reduction Principles", "RECOVERY",
                        "WHO", "https://www.who.int",
                        "Harm reduction focuses on minimizing negative health impacts of substance use without requiring abstinence. Strategies include supervised consumption, needle exchanges, and medication-assisted treatment."),
                    new ResourceItem(null, "Medication-Assisted Treatment (MAT)", "RECOVERY",
                        "SAMHSA", "https://www.samhsa.gov",
                        "MAT combines FDA-approved medications (like buprenorphine, naltrexone, methadone) with counseling. It is the gold standard for opioid use disorder and significantly reduces overdose deaths.")
                );
                resourceRepo.saveAll(resources);
            }
        };
    }
}

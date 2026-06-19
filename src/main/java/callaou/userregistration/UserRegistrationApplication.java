package callaou.userregistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * The main class for the User Registration application.
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class UserRegistrationApplication {

    /**
     * The main method to run the User Registration application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserRegistrationApplication.class, args);
    }

}
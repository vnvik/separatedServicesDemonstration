package separatedServices.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ParamAutorization {
    @NotEmpty(message = "startDate may not be empty")
    private String authorizationId;
    @NotEmpty(message = "startDate may not be empty")
    private String authorizationSurname;
    @NotEmpty(message = "startDate may not be empty")
    private String authorizationName;
    private String authorizationPathname;
    private String authorizationUnp;
    private String authorizationPost;
    private String authorizationPhone;
    private String authorizationAddress;
    private String authorizationEmail;
}

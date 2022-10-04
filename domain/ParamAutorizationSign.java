package separatedServices.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ParamAutorizationSign extends ParamAutorization {
    @NotEmpty(message = "authorizationSign may not be empty")
    private String authorizationSign;
    private String cms;
}

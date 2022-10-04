package separatedServices.domain.BenefitsCertificatePackage;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class BenefitsCertificateDataIn {
    private String pik;
    private String surname;
    private String name;
    private String patname;
    @NotEmpty(message = "childsSurname may not be empty")
    private String childsSurname;
    @NotEmpty(message = "childsName may not be empty")
    private String childsName;
    private String childsPatname;
    private String birthDate;
    @NotEmpty(message = "fromData may not be empty")
    private String fromDate;
    @NotEmpty(message = "toDate may not be empty")
    private String toDate;
}

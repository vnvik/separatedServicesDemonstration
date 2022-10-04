package separatedServices.domain.BenefitsCertificatePackage;

import lombok.Getter;
import lombok.Setter;
import separatedServices.domain.ParamAutorizationSign;

import javax.validation.Valid;

@Getter
@Setter
public class BenefitsCertificateInSign {
    @Valid
    private ParamAutorizationSign authorizationData;
    @Valid
    private BenefitsCertificateDataIn data;
}

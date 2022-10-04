package separatedServices.domain.BenefitsCertificatePackage;

import lombok.Getter;
import lombok.Setter;
import separatedServices.domain.ParamAutorization;

import javax.validation.Valid;

@Getter
@Setter
public class BenefitsCertificateIn{
    @Valid
    private ParamAutorization authorizationData;
    @Valid
    private BenefitsCertificateDataIn data;
}

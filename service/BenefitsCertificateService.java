package separatedServices.service;

import org.springframework.http.ResponseEntity;
import separatedServices.domain.BenefitsCertificatePackage.BenefitsCertificateDataIn;
import separatedServices.domain.GISSZException;

import javax.servlet.http.HttpServletRequest;

public interface BenefitsCertificateService {
    ResponseEntity<Object> getResult(BenefitsCertificateDataIn dataIn, HttpServletRequest req, String jsonParams) throws GISSZException;
}

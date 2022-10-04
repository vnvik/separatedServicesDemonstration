package separatedServices.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import separatedServices.domain.BenefitsCertificatePackage.BenefitsCertificateIn;
import separatedServices.domain.BenefitsCertificatePackage.BenefitsCertificateInSign;
import separatedServices.domain.GISSZException;
import separatedServices.service.BenefitsCertificateService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.io.StringWriter;

@RestController
@Validated
public class BenefitsCertificateController {

    private static final Logger log = Logger.getLogger("BenefitsCertificate");

    @Autowired
    private BenefitsCertificateService benefitsCertificateService;

    @PostMapping(path = "/benefitsCertificate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<Object> outMessageBenefitsCertificate(
            HttpServletRequest req,
            Authentication authentication,
            @Valid @RequestBody BenefitsCertificateIn dataIn)
            throws GISSZException, IOException {

        log.info("-----------------------------------------------------------------------");
        log.info("-START-");
        String userName = authentication.getName();
        ObjectMapper ob = new ObjectMapper();
        StringWriter wr = new StringWriter();
        ob.writeValue(wr, dataIn);
        ResponseEntity<Object> result = benefitsCertificateService.getResult(dataIn.getData(), req, wr.toString());
        log.info("---END-------");
        return result;
    }

    @PostMapping(path = "/benefitsCertificateSign", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<Object> outMessageBenefitsCertificateSign(
            HttpServletRequest req,
            Authentication authentication,
            @Valid @RequestBody BenefitsCertificateInSign dataIn)
            throws GISSZException, IOException {

        log.info("-----------------------------------------------------------------------");
        log.info("-START---WITH SIGN-");
        String userName = authentication.getName();
        ObjectMapper ob = new ObjectMapper();
        StringWriter wr = new StringWriter();
        ob.writeValue(wr, dataIn);
        ResponseEntity<Object> result = benefitsCertificateService.getResult(dataIn.getData(), req, wr.toString());
        log.info("---END WITH SIGN-------");
        return result;
    }

}

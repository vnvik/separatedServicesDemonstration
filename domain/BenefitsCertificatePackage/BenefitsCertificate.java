
package separatedServices.domain.BenefitsCertificatePackage;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import separatedServices.domain.AMonthAmount;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class BenefitsCertificate {

    private String code;
    private String name;
    private String childsName;
    private String childsBirthDate;
    private String address;
    private AMonthAmount montlyBenefits;
    private AMonthAmount onlyBenefits;
    private String fromDate;
    private String toDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone="Europe/Minsk")
    private Date requestDate;
    private BigDecimal cnes;
    private String otherInformation;
    private String closed;
    private String closeDate;

}

package separatedServices.service;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import separatedServices.domain.AMonthAmount;
import separatedServices.domain.BenefitsCertificatePackage.BenefitsCertificate;
import separatedServices.domain.BenefitsCertificatePackage.BenefitsCertificateDataIn;
import separatedServices.domain.ExceptionCustom;
import separatedServices.domain.GISSZException;
import separatedServices.domain.TMonthAmount;
import separatedServices.util.Utils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BenefitsCertificateServiceImpl implements BenefitsCertificateService{

    private static final Logger log = Logger.getLogger("BenefitsCertificate");

    @Override
    public ResponseEntity<Object> getResult(BenefitsCertificateDataIn dataIn, HttpServletRequest req, String jsonParams) throws GISSZException {
        final String serviceName = "Справка о размере пособия на детей и периоде его выплаты";
        Connection session = null;
        Utils util = new Utils();
        List data = new ArrayList<>();
        String method = "BenefitsCertificate";
        BenefitsCertificate benefitsCertificate = new BenefitsCertificate();

        log.info("---HEADER START");
        Enumeration<String> param = req.getParameterNames();
        Enumeration<String> header = req.getHeaderNames();
        while (header.hasMoreElements()) {
            String headerName = header.nextElement();
            String headerValue = req.getHeader(headerName);
            log.info(headerName + " = " + headerValue);
        }
        log.info("---HEADER END");
        log.info("---PARAM START");
        log.info(jsonParams);
        log.info("---PARAM END");

        AMonthAmount tempAmount = new AMonthAmount(); //ВРЕМЕННОЕ ПОЛЕ ЧТО БЫ ПОСОБИЯ НА ДЕТЕЙ С 3 ДО 18 В
        //ПЕРИОД ВОСПИТАНИЯ ДО 3Х ЛЕТ БЫЛИ В КОНЦЕ. СООТВЕТСТВЕННО ПОСЛЕ ПОСОБИЙ ДО 3Х ЛЕТ
        try {
            String pik = dataIn.getPik();
            String surname = dataIn.getSurname();
            String name = dataIn.getName();
            String patname = dataIn.getPatname();
            String childsSurname = dataIn.getChildsSurname();
            String childsName = dataIn.getChildsName();
            String childsPatname = dataIn.getChildsPatname();
            String birthDate = dataIn.getBirthDate();
            String fromDate = dataIn.getFromDate();
            String toDate = dataIn.getToDate();
            if (birthDate == null) {
                birthDate = "1799-12-01";
            }
            data.addAll(Arrays.asList(pik, surname, name, patname, childsSurname, childsName, childsPatname, birthDate, fromDate, toDate));
            log.info("try with data:" + data);
            session = util.authorize(method);
            log.info("Сonnection established");

            PreparedStatement st1 = session.prepareStatement(
                    "SELECT * FROM TABLE "
                            + "( "
                            + "  WEB_PACKAGE.SERVICEMETHOD1CASE "
                            + "  ( "
                            + "    :1, :2, :3, :4, "
                            + "    TO_DATE(:5,'YYYY-MM-DD'), "
                            + "    :6, :7, :8 "
                            + "  )"
                            + ") "
            );
            st1.setString(1, surname);
            st1.setString(2, name);
            st1.setString(3, patname);
            st1.setString(4, pik);
            st1.setString(5, birthDate);
            st1.setString(6, childsSurname);
            st1.setString(7, childsName);
            st1.setString(8, childsPatname);

            ResultSet rs1 = st1.executeQuery();
            long case_count = 0;
            AMonthAmount montlyBenefits = new AMonthAmount();
            AMonthAmount onlyBenefits = new AMonthAmount();

            while (rs1.next()) {

                case_count++;

                benefitsCertificate.setCode(rs1.getString("F_CODE"));
                benefitsCertificate.setName(rs1.getString("FNAME"));
                benefitsCertificate.setAddress(rs1.getString("FADDRESS"));
                benefitsCertificate.setChildsName(rs1.getString("FNAMECHILD"));
                benefitsCertificate.setChildsBirthDate(rs1.getString("FBIRTHDATE"));
                log.info("barier1");
                PreparedStatement st2 = session.prepareStatement(
                        "SELECT * FROM TABLE "
                                + "( "
                                + "   WEB_PACKAGE.SERVICEMETHOD1_MONTHLYBENEFITS "
                                + "   ( "
                                + "     :1, :2, "
                                + "     TO_DATE(:3,'YYYY-MM-DD'), "
                                + "     TO_DATE(:4,'YYYY-MM-DD')"
                                + "   ) "
                                + ") "
                );

                st2.setString(1, rs1.getString("FCID"));
                st2.setString(2, rs1.getString("FPID"));
                int year = Integer.parseInt(fromDate.substring(0, 4));
                if (year < 2015) {
                    fromDate = "2015-01-01";
                }
                st2.setString(3, fromDate);
                st2.setString(4, toDate);
                ResultSet rs2 = st2.executeQuery();
                long row1_count = 1;
                while (rs2.next()) {
                    TMonthAmount benefit2 = new TMonthAmount();
                    benefit2.setSvId(case_count);
                    benefit2.setRow(row1_count);
                    benefit2.setMonth(rs2.getInt("F_MONTH"));
                    benefit2.setYear(rs2.getInt("F_YEAR"));
                    benefit2.setStartDate(rs2.getString("F_ST_DATE"));
                    benefit2.setEndDate(rs2.getString("F_END_DATE"));
                    benefit2.setTssr(rs2.getString("F_TSSR"));
                    benefit2.setValue(new BigDecimal(rs2.getString("F_VALUE")));
                    benefit2.setText(rs2.getString("F_TEXT"));
                    if (benefit2.getText().equals("Пособие семьям на детей в возрасте от 3 до 18 лет в период воспитания ребенка в возрасте до 3 лет")) {
                        tempAmount.getSvItems().add(benefit2);
                    } else {
                        montlyBenefits.getSvItems().add(benefit2);
                    }
                    row1_count++;
                }

                PreparedStatement st3 = session.prepareStatement(
                        "SELECT * FROM TABLE "
                                + "( "
                                + "   WEB_PACKAGE.SERVICEMETHOD1_ONLYBENEFITS "
                                + "   ( "
                                + "     :1, :2, "
                                + "     TO_DATE(:3,'YYYY-MM-DD'), "
                                + "     TO_DATE(:4,'YYYY-MM-DD') "
                                + "   ) "
                                + ") "
                );

                st3.setString(1, rs1.getString("FCID"));
                st3.setString(2, rs1.getString("FPID"));
                st3.setString(3, fromDate);
                st3.setString(4, toDate);

                ResultSet rs3 = st3.executeQuery();

                long row2_count = 1;
                while (rs3.next()) {
                    TMonthAmount benefit3 = new TMonthAmount();
                    benefit3.setSvId(case_count);
                    benefit3.setRow(row2_count);
                    benefit3.setMonth(rs3.getInt("F_MONTH"));
                    benefit3.setYear(rs3.getInt("F_YEAR"));
                    benefit3.setStartDate(rs3.getString("F_ST_DATE"));
                    benefit3.setEndDate(rs3.getString("F_END_DATE"));
                    benefit3.setTssr(rs3.getString("F_TSSR"));
                    benefit3.setValue(new BigDecimal(rs3.getString("F_VALUE")));
                    benefit3.setText(rs3.getString("F_TEXT"));
                    onlyBenefits.getSvItems().add(benefit3);
                    row2_count++;
                }
                PreparedStatement st4 = session.prepareStatement("select WEB_PACKAGE.SERVICEMETHOD1_CHAESBENEFITS(:1,:2,to_date(:3,'YYYY-MM-DD'),to_date(:4,'YYYY-MM-DD')) F_CNES from dual");
                st4.setString(1, rs1.getString("FCID"));
                st4.setString(2, rs1.getString("FPID"));
                st4.setString(3, fromDate);
                st4.setString(4, toDate);

                ResultSet rs4 = st4.executeQuery();
                while (rs4.next())
                    benefitsCertificate.setCnes(rs4.getBigDecimal("F_CNES"));
                PreparedStatement st5 = session.prepareStatement("select WEB_PACKAGE.servicemethod1_DopSved(:1,:2,to_date(:3,'YYYY-MM-DD'),to_date(:4,'YYYY-MM-DD')) F_OtherInformation from dual");

                st5.setString(1, rs1.getString("FCID"));
                st5.setString(2, rs1.getString("FPID"));
                st5.setString(3, fromDate);
                st5.setString(4, toDate);

                ResultSet rs5 = st5.executeQuery();
                while (rs5.next())
                    benefitsCertificate.setOtherInformation(rs5.getString("F_OtherInformation"));
                log.info("barier2");
                PreparedStatement st6 = session.prepareStatement("select WEB_PACKAGE.servicemethod1_Zakrito(:1,:2,to_date(:3,'YYYY-MM-DD'),to_date(:4,'YYYY-MM-DD')) F_Closed from dual");

                st6.setString(1, rs1.getString("FCID"));
                st6.setString(2, rs1.getString("FPID"));
                st6.setString(3, fromDate);
                st6.setString(4, toDate);

                ResultSet rs6 = st6.executeQuery();
                while (rs6.next())
                    benefitsCertificate.setClosed(rs6.getString("F_Closed"));
                log.info("barier3");
                PreparedStatement st7 = session.prepareStatement("select WEB_PACKAGE.servicemethod1_DatZakr(:1,:2,to_date(:3,'YYYY-MM-DD'),to_date(:4,'YYYY-MM-DD')) F_CloseDate from dual");

                st7.setString(1, rs1.getString("FCID"));
                st7.setString(2, rs1.getString("FPID"));
                st7.setString(3, fromDate);
                st7.setString(4, toDate);

                ResultSet rs7 = st7.executeQuery();
                while (rs7.next())
                    benefitsCertificate.setCloseDate(rs7.getString("F_CloseDate"));
                log.info("end of iteration");
            }

            benefitsCertificate.setRequestDate(new Date());
            SimpleDateFormat format_date = new SimpleDateFormat("YYYY-MM-dd");
            String date = fromDate;
            String temp = "";
            temp += date.substring(0, 8) + "01";
            benefitsCertificate.setFromDate(temp);
            date = toDate;
            temp = "";
            switch (date.substring(5, 7)) {
                case "01":
                case "03":
                case "05":
                case "07":
                case "08":
                case "10":
                case "12":
                    temp += date.substring(0, 8) + "31";
                    break;
                case "02":
                    int year_case = Integer.parseInt(date.substring(0, 4));
                    if (((year_case % 4 == 0) &&
                            !(year_case % 100 == 0))
                            || (year_case % 400 == 0))
                        temp += date.substring(0, 8) + "29";
                    else
                        temp += date.substring(0, 8) + "28";
                    break;
                case "04":
                case "06":
                case "09":
                case "11":
                    temp += date.substring(0, 8) + "30";
                    break;
            }
            benefitsCertificate.setToDate(temp);
            montlyBenefits.getSvItems().addAll(tempAmount.getSvItems());
            benefitsCertificate.setMontlyBenefits(montlyBenefits);
            benefitsCertificate.setOnlyBenefits(onlyBenefits);



        } catch (SQLException ex) {
            log.error(ex.getMessage());
            switch (ex.getErrorCode()) {
                case 20401:
                    log.error(data + Integer.toString(Utils.Err401.getCode()) + Utils.Err401.getMessage());
                    return new ResponseEntity<>(new ExceptionCustom(206, "Неоднозначный результат поиска"), HttpStatus.valueOf(206));
                case 20402:
                    log.error(data + Integer.toString(Utils.Err402.getCode()) + Utils.Err402.getMessage());
                    return new ResponseEntity<>(new ExceptionCustom(204, "Данных не найдено"), HttpStatus.valueOf(204));
                case 20403:
                    log.error(data + Integer.toString(Utils.Err403.getCode()) + Utils.Err403.getMessage());
                    throw Utils.Err403;
                default:
                    log.error(data + Integer.toString(Utils.Err497.getCode()) + Utils.Err497.getMessage());
                    throw Utils.Err497;
            }
        } catch (GISSZException ex) {
            log.error(data + Integer.toString(ex.getCode()) + Utils.ExceptionToString(ex));
//                Utils.LogWrite(data, Integer.toString(ex.getCode()), Utils.ExceptionToString(ex));
//                Utils.LogWriteDB(serviceName, data, Integer.toString(ex.getCode()), Utils.ExceptionToString(ex));
            throw ex;
        } catch (Exception ex) {
            log.error(data + "500" + Utils.ExceptionToString(ex));

            throw new GISSZException(ex);
        } finally {
            try {
                session.close();
                log.info("connection close");
            } catch (SQLException ex) {
                log.error("Не удалось закрыть основную сессию" + Utils.ExceptionToString(ex));
            } catch (Exception ex) {
                log.error("error conection closed" + ex);
            }
        }
        log.info(serviceName + data);
        return new ResponseEntity<>(benefitsCertificate, HttpStatus.OK);
    }
}

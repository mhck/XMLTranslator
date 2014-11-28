package banktwotranslator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author mhck
 */
public class DateCalculator {
    public static String translateDate(String xmlMessage) {
        String startTarget = "<loanDuration>";
        String endTarget = "</loanDuration>";
        int startIndex = xmlMessage.indexOf(startTarget) + startTarget.length();
        int endIndex = xmlMessage.indexOf(endTarget);
        Calendar date = new GregorianCalendar(1970, 0, 1);
        int loanDurationDays = Integer.parseInt(xmlMessage.substring(startIndex, endIndex));
        date.add(Calendar.DAY_OF_YEAR, loanDurationDays);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss zzz");
        String dateFormatted = fmt.format(date.getTime());
        String resultMessage = xmlMessage.substring(0, startIndex) + dateFormatted + xmlMessage.substring(endIndex);
        return resultMessage;
    }
}
package am.ik.home;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class Outcome implements Serializable {
    private String outcomeName;
    private Integer amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outcomeDate;
    private String outcomeBy;
}

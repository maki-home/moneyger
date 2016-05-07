package am.ik.home;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class Outcome implements Serializable {
    private String outcomeName;
    private Integer amount;
    private LocalDate outcomeDate;
    private String outcomeBy;
}

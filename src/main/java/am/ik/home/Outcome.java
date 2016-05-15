package am.ik.home;

import lombok.Data;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

@Data
public class Outcome implements Serializable {
    private String outcomeName;
    private Integer amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outcomeDate;
    private String outcomeBy;

    @Setter
    private Map<String, String> memberMap;

    public String getMemberName() {
        if (this.memberMap == null || !memberMap.containsKey(this.outcomeBy)) {
            return "System";
        }
        return this.memberMap.get(this.outcomeBy);
    }
}

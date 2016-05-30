package am.ik.home;

import lombok.Data;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

@Data
public class Income implements Serializable {
    private Integer incomeId;
    @NotEmpty
    private String incomeName;
    @NotNull
    @Min(0)
    @Max(10000000)
    private Integer amount;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate incomeDate;
    private String incomeBy;
    @NotNull
    @Valid
    private IncomeCategory incomeCategory;

    @Setter
    private Map<String, String> memberMap;

    public String getMemberName() {
        if (this.memberMap == null || !memberMap.containsKey(this.incomeBy)) {
            return "System";
        }
        return this.memberMap.get(this.incomeBy);
    }

    public Integer getCategoryId() {
        return this.incomeCategory == null ? null : this.incomeCategory.getCategoryId();
    }

    public String getCategoryName() {
        return this.incomeCategory == null ? null : this.incomeCategory.getCategoryName();
    }

    @Data
    public static class IncomeCategory implements Serializable {
        @NotNull
        private Integer categoryId;
        private String categoryName;
    }
}

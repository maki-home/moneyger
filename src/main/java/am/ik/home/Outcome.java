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
public class Outcome implements Serializable {
    private Integer outcomeId;
    @NotEmpty
    private String outcomeName;
    @NotNull
    @Min(0)
    @Max(10000000)
    private Integer amount;
    @NotNull
    @Min(0)
    @Max(999)
    private Integer quantity;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outcomeDate;
    private String outcomeBy;
    @NotNull
    @Valid
    private OutcomeCategory outcomeCategory;
    private boolean isCreditCard;

    @Setter
    private Map<String, String> memberMap;

    public String getMemberName() {
        if (this.memberMap == null || !memberMap.containsKey(this.outcomeBy)) {
            return "System";
        }
        return this.memberMap.get(this.outcomeBy);
    }

    public Integer getCategoryId() {
        return this.outcomeCategory == null ? null : this.outcomeCategory.getCategoryId();
    }

    public String getCategoryName() {
        return this.outcomeCategory == null ? null : this.outcomeCategory.getCategoryName();
    }

    public Integer getParentCategoryId() {
        ParentOutcomeCategory parent = this.outcomeCategory == null ? null : this.outcomeCategory.getParentOutcomeCategory();
        return parent == null ? null : parent.getParentCategoryId();
    }

    public String getParentCategoryName() {
        ParentOutcomeCategory parent = this.outcomeCategory == null ? null : this.outcomeCategory.getParentOutcomeCategory();
        return parent == null ? null : parent.getParentCategoryName();
    }

    @Data
    public static class OutcomeCategory implements Serializable {
        @NotNull
        private Integer categoryId;
        private String categoryName;
        private ParentOutcomeCategory parentOutcomeCategory;
    }

    @Data
    public static class ParentOutcomeCategory implements Serializable {
        private Integer parentCategoryId;
        private String parentCategoryName;
    }

    @Data
    public static class SummaryByDate implements Serializable {
        private LocalDate outcomeDate;
        private Long subTotal;
    }

    @Data
    public static class SummaryByParentCategory implements Serializable {
        private Integer parentCategoryId;
        private String parentCategoryName;
        private Long subTotal;
    }
}

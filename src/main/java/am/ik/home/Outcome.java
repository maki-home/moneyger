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
    private OutcomeCategory outcomeCategory;

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
        private Integer categoryId;
        private String categoryName;
        private ParentOutcomeCategory parentOutcomeCategory;
    }

    @Data
    public static class ParentOutcomeCategory implements Serializable {
        private Integer parentCategoryId;
        private String parentCategoryName;
    }
}

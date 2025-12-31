package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {
    private int Id;
    private String Name;

    private boolean RequiresNumber;
    private String NumberPrefix;
    private Integer SortOrder;
}

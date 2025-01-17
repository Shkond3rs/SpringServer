package shkond.server.request.school;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
/* Данные для добавления школы и учителей*/
public class AddSchoolRequest {
    private String schoolName;
    private Long artCategoryId;
    private String description;
    private String city;
    private String street;
    private List<Long> programsId;
}

package sa.cerebra.task.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileModel {
    private String name;
    private String relativePath;
    private LocalDateTime uploadDate;
}

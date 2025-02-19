package models;

import lombok.*;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Data
public class Log {

    private LocalTime timestamp;
    private String jobDescription;
    private LogStatus status;
    private int pid;
}

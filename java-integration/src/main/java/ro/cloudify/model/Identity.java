package ro.cloudify.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Identity {
    private List<String> methods;
    @JsonProperty("application_credential")
    private ApplicationCredentials applicationCredentials;
}

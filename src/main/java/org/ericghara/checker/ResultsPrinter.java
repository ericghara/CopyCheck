package org.ericghara.checker;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ResultsPrinter implements ApplicationListener<ApplicationReadyEvent> {

    private final FileChecker checker;



    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var valid = checker.getValid();
        var invalid = checker.getInvalid();
        valid.forEach( (r) -> System.out.printf("%-9s%s%n", "Valid:", r.path() ) );
        invalid.forEach( (r) -> System.out.printf("%-9s%s%n", "Invalid:", r.path() ) );
        System.out.printf("%nSummary: %d of %d are valid%n", valid.size(), valid.size()+invalid.size() );
    }
}

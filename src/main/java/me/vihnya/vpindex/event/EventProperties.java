package me.vihnya.vpindex.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class EventProperties {

    private final Parameter defaultParameter;

    private final List<Parameter> additionalParameters;

}

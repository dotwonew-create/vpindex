package me.vihnya.vpindex.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class Pair<L, R> {

    private final L left;

    private final R right;

}

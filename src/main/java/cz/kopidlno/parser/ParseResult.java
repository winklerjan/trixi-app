package cz.kopidlno.parser;

import cz.kopidlno.model.CastObce;
import cz.kopidlno.model.Obec;

import java.util.List;

public record ParseResult(List<Obec> obce, List<CastObce> castiObci) {
}

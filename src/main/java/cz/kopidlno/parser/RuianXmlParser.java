package cz.kopidlno.parser;

import cz.kopidlno.model.CastObce;
import cz.kopidlno.model.Obec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * StAX-based parser for RUIAN VFR (Výměnný Formát RÚIAN) XML files.
 * Extracts obce and části obce without loading the full document into memory.
 */
@Component
@Slf4j
public class RuianXmlParser {

    private static final String NS_VF  = "urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1";
    private static final String NS_OBI = "urn:cz:isvs:ruian:schemas:ObecIntTypy:v1";
    private static final String NS_COI = "urn:cz:isvs:ruian:schemas:CastObceIntTypy:v1";

    private enum Context {
        NONE,
        OBEC,
        CAST_OBCE,
        CAST_OBCE_OBEC_REF  // inside coi:Obec element within a CastObce
    }

    public ParseResult parse(InputStream inputStream) throws XMLStreamException {
        List<Obec> obce = new ArrayList<>();
        List<CastObce> castiObci = new ArrayList<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        // Disable external entity resolution to prevent XXE
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        Context context = Context.NONE;
        String currentField = null;
        Obec currentObec = null;
        CastObce currentCastObce = null;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String ns = reader.getNamespaceURI();
                    String local = reader.getLocalName();

                    if (NS_VF.equals(ns) && "Obec".equals(local)) {
                        context = Context.OBEC;
                        currentObec = new Obec();

                    } else if (NS_VF.equals(ns) && "CastObce".equals(local)) {
                        context = Context.CAST_OBCE;
                        currentCastObce = new CastObce();

                    } else if (context == Context.CAST_OBCE && NS_COI.equals(ns) && "Obec".equals(local)) {
                        // reference to parent obec inside cast_obce
                        context = Context.CAST_OBCE_OBEC_REF;

                    } else if (context == Context.OBEC && NS_OBI.equals(ns) && "Kod".equals(local)) {
                        currentField = "OBEC_KOD";

                    } else if (context == Context.OBEC && NS_OBI.equals(ns) && "Nazev".equals(local)) {
                        currentField = "OBEC_NAZEV";

                    } else if (context == Context.CAST_OBCE && NS_COI.equals(ns) && "Kod".equals(local)) {
                        currentField = "CAST_KOD";

                    } else if (context == Context.CAST_OBCE && NS_COI.equals(ns) && "Nazev".equals(local)) {
                        currentField = "CAST_NAZEV";

                    } else if (context == Context.CAST_OBCE_OBEC_REF && NS_OBI.equals(ns) && "Kod".equals(local)) {
                        currentField = "CAST_OBEC_KOD";
                    }
                }

                case XMLStreamConstants.CHARACTERS -> {
                    if (currentField == null) break;
                    String text = reader.getText().trim();
                    if (text.isEmpty()) break;

                    switch (currentField) {
                        case "OBEC_KOD"      -> currentObec.setKod(Long.parseLong(text));
                        case "OBEC_NAZEV"    -> currentObec.setNazev(text);
                        case "CAST_KOD"      -> currentCastObce.setKod(Long.parseLong(text));
                        case "CAST_NAZEV"    -> currentCastObce.setNazev(text);
                        case "CAST_OBEC_KOD" -> currentCastObce.setObecKod(Long.parseLong(text));
                    }
                    currentField = null;
                }

                case XMLStreamConstants.END_ELEMENT -> {
                    String ns = reader.getNamespaceURI();
                    String local = reader.getLocalName();

                    if (NS_VF.equals(ns) && "Obec".equals(local)) {
                        log.debug("Parsed obec: {} - {}", currentObec.getKod(), currentObec.getNazev());
                        obce.add(currentObec);
                        currentObec = null;
                        context = Context.NONE;

                    } else if (NS_VF.equals(ns) && "CastObce".equals(local)) {
                        log.debug("Parsed část obce: {} - {}", currentCastObce.getKod(), currentCastObce.getNazev());
                        castiObci.add(currentCastObce);
                        currentCastObce = null;
                        context = Context.NONE;

                    } else if (NS_COI.equals(ns) && "Obec".equals(local)) {
                        context = Context.CAST_OBCE;
                    }

                    currentField = null;
                }
            }
        }

        reader.close();
        return new ParseResult(obce, castiObci);
    }
}

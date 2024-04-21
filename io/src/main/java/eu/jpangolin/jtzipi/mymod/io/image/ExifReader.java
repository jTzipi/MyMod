package eu.jpangolin.jtzipi.mymod.io.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

public final class ExifReader {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExifReader.class);

    private ExifReader() {

    }

    /**
     * Liest die - wenn vorhanden - Tag Information zum Typ "Orientation".
     * @param inputStream Input Stream
     * @return den Tag oder das leere Optional wenn nicht gefunden.
     * @throws IOException Fehler beim Lesen
     * @throws NullPointerException if {@code inputStream} ist null
     * @see #parseTagOrientation(java.io.File)
     */
    public static Optional<ExifTagOrientation> parseTagOrientation( final InputStream inputStream ) throws IOException {
        Objects.requireNonNull(inputStream, "Die Datei zu parsen ist null!");


        try {
            return parseOrientation( ImageMetadataReader.readMetadata( inputStream ) );
        } catch ( ImageProcessingException ipe ) {
            LOG.warn( "Interner Fehler bei Tag Lesen", ipe);
            throw new IOException( "Fehler beim Lesen der Tags", ipe.getCause() );
        }
    }

    /**
     * Liest die - wenn vorhanden - Tag Information zum Typ "Orientation".
     * @param imageFile Bild Datei
     * @return Den Tag wenn gefunden. Sonst das leere Optional.
     * @throws IOException Fehler beim Lesen
     * @throws NullPointerException if {@code imageFile} is null
     * @see #parseTagOrientation(java.io.InputStream)
     */

    public static Optional<ExifTagOrientation> parseTagOrientation( final java.io.File imageFile ) throws  IOException {
        Objects.requireNonNull( imageFile );

        return parseTagOrientation( Files.newInputStream( imageFile.toPath() ) );
    }

    /**
     * Suche eine Datei nach Exif Tag-Orientation.
     *
     * @param meta
     *            Metadata obtained from {@link ImageMetadataReader}
     * @return Das Orientation-Tag wenn Exif Tag vorhanden. Sonst das leere Optional wenn keine Exif Daten vorliegen.
     *
     * @throws ImageProcessingException
     *             Fehler beim Tag Lesen
     *
     *
     */

    static Optional<ExifTagOrientation> parseOrientation(Metadata meta) throws ImageProcessingException {

        // Versuche das Exif Feld zu betreten
        ExifIFD0Directory directory = meta.getFirstDirectoryOfType(ExifIFD0Directory.class);

        // keine Exif Metadaten oder kein Orientation Tag
        if (null == directory || !directory.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
            LOG.warn("Keine Metadaten oder kein Tag 'Orientation'");
            return Optional.empty();
        }

        int val = directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);

        return Optional.of(ExifTagOrientation.of(val));
    }

}

package bittorrent.app.converters;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.util.StringConverter;

/**
 * Converts a java nio path to a string and back
 */
public class PathConverter extends StringConverter<Path>
{
	@Override
	public String toString(Path path)
	{
		return path.toString();
	}

	@Override
	public Path fromString(String string)
	{
		return Paths.get(string);
	}
}

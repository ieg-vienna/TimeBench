package timeBench.calendar.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "granularityIdentifier")
@XmlAccessorType(XmlAccessType.FIELD)
public class GranularityIdentifier {

	@XmlAttribute(required = true)
	private Integer identifier;

	@XmlAttribute(required = true)
	private Integer typeIdentifier;

	public GranularityIdentifier(){
	}

	public GranularityIdentifier(Integer identifier, Integer typeIdentifier) {
		this.identifier = identifier;
		this.typeIdentifier = typeIdentifier;
	}

	public int getIdentifier() {
		return identifier;
	}

	public int getTypeIdentifier() {
		return typeIdentifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GranularityIdentifier that = (GranularityIdentifier) o;

		return typeIdentifier.equals(that.typeIdentifier) && identifier.equals(that.identifier);
	}

	@Override
	public int hashCode() {
		int result = identifier.hashCode();
		result = 31 * result + typeIdentifier.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "GranularityIdentifier{" +
				"identifier=" + identifier +
				", typeIdentifier=" + typeIdentifier +
				'}';
	}
}

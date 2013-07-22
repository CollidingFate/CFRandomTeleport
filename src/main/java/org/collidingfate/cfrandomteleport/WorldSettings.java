package org.collidingfate.cfrandomteleport;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import com.google.common.collect.ImmutableMap;

@SerializableAs("CFRandomTeleport-World")
public class WorldSettings implements ConfigurationSerializable {

	private static final String SHAPE = "shape";
	private static final String CENTER_X = "center-x";
	private static final String CENTER_Z = "center-z";
	private static final String RADIUS = "radius";
	
	public enum Shape {
		CIRCLE,
		SQUARE;
	}
	
	private final Shape shape;
	private final double centerX;
	private final double centerZ;
	private final double radius;
	
	public WorldSettings(Shape shape, double centerX, double centerZ, double radius) {
		this.shape = shape;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.radius = radius;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public double getCenterX() {
		return centerX;
	}
	
	public double getCenterZ() {
		return centerZ;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public static ConfigurationSerializable deserialize(Map<String, Object> map) {
		Shape shape = Shape.valueOf((String) map.get(SHAPE));
		if (shape == null) {
			StringBuilder sb = new StringBuilder(128);
			sb.append(map.get(SHAPE)).append(" is not a valid Shape. Must be one of: ");
			for (Shape validShapes : Shape.values()) {
				sb.append(validShapes.name()).append(" ");
			}
			sb.append("(Case-sensitive).");
			throw new IllegalArgumentException(sb.toString());
		}

		Double centerX = (Double) map.get(CENTER_X);
		Validate.notNull(centerX, "One of the worlds doesn't have a valid %1$s setting.", CENTER_X);
		
		Double centerZ = (Double) map.get(CENTER_Z);
		Validate.notNull(centerZ, "One of the worlds doesn't have a valid %1$s setting.", CENTER_Z);
		
		Double radius = (Double) map.get(RADIUS);
		Validate.notNull(radius, "One of the worlds doesn't have a valid %1$s setting.", RADIUS);
		
		return new WorldSettings(shape, centerX, centerZ, radius);
	}
	
	@Override
    public Map<String, Object> serialize() {
		return ImmutableMap.<String, Object>of(
			SHAPE, shape.name(),
			CENTER_X, centerX,
			CENTER_Z, centerZ,
			RADIUS, radius
		);
    }

}

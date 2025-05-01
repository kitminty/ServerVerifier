package org.kitminty;
import java.util.List;

public record Masscan(String ip, List<Port> ports) {}
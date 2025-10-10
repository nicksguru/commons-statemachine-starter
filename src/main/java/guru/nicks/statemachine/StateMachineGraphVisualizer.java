package guru.nicks.statemachine;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Renders state machine graph in various {@link Format formats} supported by GraphViz.
 *
 * @param <S> state type
 * @param <E> event type
 */
public interface StateMachineGraphVisualizer<S, E> {

    /**
     * Returns state machine factory. Needed for {@link #renderStateMachineGraph(Format)}.
     *
     * @return state machine factory
     */
    StateMachineFactory<S, E> getStateMachineFactory();

    /**
     * Returns content type for the specified state machine graph format.
     *
     * @param format output format for graph visualization
     * @return content type
     * @see #renderStateMachineGraph(Format)
     */
    static ContentType getStateMachineGraphContentType(Format format) {
        return switch (format) {
            case PNG -> ContentType.IMAGE_PNG;
            case SVG, SVG_STANDALONE -> ContentType.IMAGE_SVG;
            // JSON, DOT, etc.
            default -> ContentType.TEXT_PLAIN;
        };
    }

    /**
     * Returns link color for the state machine graph.
     *
     * @return link color (default is {@code #B6B5D8})
     */
    default Color getStateMachineGraphLinkColor() {
        return Color.rgb("B6B5D8");
    }

    /**
     * Renders state machine graph in the specified format using GraphViz.
     *
     * @param format output format for graph visualization
     * @return content type and content data: text for SVG, binary for PNG, etc.
     * @see #getStateMachineGraphContentType(Format)
     */
    default Pair<ContentType, byte[]> renderStateMachineGraph(Format format) {
        // build new empty state machine in order to collect its state transitions
        StateMachine<S, E> stateMachine = getStateMachineFactory().getStateMachine();

        // collect outbound transitions for each state
        Map<S, List<Transition<S, E>>> transitionsGroupedBySourceState = stateMachine.getTransitions()
                .stream()
                .collect(Collectors.groupingBy(transition -> transition.getSource().getId()));

        var graph = Factory.mutGraph().setDirected(true);

        transitionsGroupedBySourceState.forEach((sourceState, transitions) -> {
            Node sourceNode = createStateMachineGraphNode(sourceState);

            transitions.stream()
                    .map(transition -> {
                        S targetState = transition.getTarget().getId();
                        Node targetNode = createStateMachineGraphNode(targetState);

                        E event = transition.getTrigger().getEvent();
                        Label label = Label.of(getStateMachineGraphEventName(event));

                        return sourceNode.link(
                                Link.to(targetNode).with(label, getStateMachineGraphLinkColor()));
                    }).forEach(graph::add);
        });

        // prevent node overlaps; see https://graphviz.org/docs/attrs/overlap/
        graph.graphAttrs().add("overlap", "false");
        Graphviz graphviz = Graphviz.fromGraph(graph);
        ContentType contentType = StateMachineGraphVisualizer.getStateMachineGraphContentType(format);

        try (var buffer = new ByteArrayOutputStream()) {
            graphviz.render(format).toOutputStream(buffer);
            return Pair.of(contentType, buffer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error rendering graph: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a graph node for the specified state and the following customized attributes:
     * <ul>
     *     <li>shape: {@link Shape#ELLIPSE}</li>
     *     <li>style: {@link Style#FILLED}</li>
     *     <li>fill color: {@code #D6CADD}</li>
     *     <li>margin: {@code 0.7,0.0} (horizontal gap between nodes makes link labels further from each other -
     *         needed to avoid label overlap</li>
     * </ul>
     *
     * @param state state
     * @return graph node
     */
    default Node createStateMachineGraphNode(S state) {
        return Factory
                .node(getStateMachineGraphStateName(state))
                .with(Shape.ELLIPSE,
                        Style.FILLED,
                        Color.rgb("D6CADD").fill(),
                        Attributes.attr("margin", "0.7,0.0"));
    }

    private String getStateMachineGraphStateName(S state) {
        return state instanceof Enum<?> en
                ? en.name()
                : Objects.toString(state, "<null>");
    }

    private String getStateMachineGraphEventName(E event) {
        // lowercase enum member name
        return event instanceof Enum<?> en
                ? en.name().toLowerCase()
                : Objects.toString(event, "<null>");
    }

}

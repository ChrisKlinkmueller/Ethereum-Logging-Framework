package au.csiro.data61.aap.elf;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

public final class Pipeline<T> {
    private final MutableGraph<T> dag;

    public Pipeline() {
        this.dag = GraphBuilder.directed().build();
    }

    public void addTask(T task) {
        checkNotNull(task);
        checkArgument(!this.dag.nodes().contains(task));
        this.dag.addNode(task);
    }

    public void removeTask(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        this.dag.removeNode(task);
    }

    public Set<T> getSourceTasks() {
        return this.sourceTaskStream().collect(Collectors.toSet());
    }

    public Stream<T> sourceTaskStream() {
        return this.dag.nodes().stream().filter(this::isSource);
    }

    public Set<T> getSinkTasks() {
        return this.sinkTaskStream().collect(Collectors.toSet());
    }

    public Stream<T> sinkTaskStream() {
        return this.dag.nodes().stream().filter(this::isSink);
    }

    public boolean isSource(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        return this.dag.inDegree(task) == 0;
    }

    public boolean isSink(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        return this.dag.outDegree(task) == 0;
    }

    public Set<T> getPredecessors(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        return this.dag.predecessors(task);
    }

    public Stream<T> predecessorStream(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        return this.dag.predecessors(task).stream();
    }

    public Set<T> getSuccessors(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        return this.dag.successors(task);
    }

    public Stream<T> successorStream(T task) {
        checkNotNull(task);
        checkArgument(this.dag.nodes().contains(task));
        return this.dag.successors(task).stream();
    }

    public void connectTasks(T source, T target) {
        checkNotNull(source);
        checkArgument(this.dag.nodes().contains(source));
        checkNotNull(target);
        checkArgument(this.dag.nodes().contains(target));
        checkArgument(!this.dag.hasEdgeConnecting(source, target));
        this.dag.putEdge(source, target);
    }

    public void insertTaskSequence(List<T> sequence) {
        checkArgument(2 <= sequence.size());
        checkArgument(sequence.stream().allMatch(Objects::nonNull));
        checkArgument(sequence.stream().allMatch(this.dag.nodes()::contains));
        for (int i = 0; i < sequence.size() - 1; i++) {
            checkArgument(!this.dag.hasEdgeConnecting(sequence.get(i), sequence.get(i + 1)));
            this.dag.putEdge(sequence.get(i), sequence.get(i + 1));
        }
    }
    
}

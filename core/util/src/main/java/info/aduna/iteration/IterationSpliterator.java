package info.aduna.iteration;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * A {@link Spliterator} implementation that wraps an {@link Iteration}. It
 * handles occurrence of checked exceptions by wrapping them in
 * RuntimeExceptions, and in addition ensures that the wrapped Iteration is
 * closed when exhausted (if it's a {@link CloseableIteration}).
 *
 * @author Jeen Broekstra
 * @since 4.0
 */
public class IterationSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

	private final Iteration<T, ? extends Exception> iteration;

	/**
	 * Creates a {@link Spliterator} implementation that wraps the supplied
	 * {@link Iteration}. It handles occurrence of checked exceptions by wrapping
	 * them in RuntimeExceptions, and in addition ensures that the wrapped
	 * Iteration is closed when exhausted (if it's a {@link CloseableIteration}).
	 * 
	 * @param iteration
	 *        the iteration to wrap
	 */
	public IterationSpliterator(final Iteration<T, ? extends Exception> iteration) {
		super(Long.MAX_VALUE, Spliterator.IMMUTABLE | Spliterator.NONNULL);
		this.iteration = iteration;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		try {
			if (iteration.hasNext()) {
				action.accept(iteration.next());
				return true;
			}
			Iterations.closeCloseable(iteration);
			return false;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void forEachRemaining(final Consumer<? super T> theAction) {
		try {
			while (iteration.hasNext()) {
				theAction.accept(iteration.next());
			}
			Iterations.closeCloseable(iteration);
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

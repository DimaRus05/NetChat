package edu.hse.netchat.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class LimitedLineReaderTest {

    @Test
    void returnsNullOnEof() throws Exception {
        LimitedLineReader reader = new LimitedLineReader(new StringReader(""), 10);

        assertThat(reader.readLine()).isNull();
    }

    @Test
    void returnsLastLineOnEofWithoutNewline() throws Exception {
        LimitedLineReader reader = new LimitedLineReader(new StringReader("abc"), 10);

        assertThat(reader.readLine()).isEqualTo("abc");
        assertThat(reader.readLine()).isNull();
    }

    @Test
    void rejectsTooLongLine() {
        LimitedLineReader reader = new LimitedLineReader(new StringReader("abcd\n"), 3);

        assertThatThrownBy(reader::readLine)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("exceeds limit");
    }
}

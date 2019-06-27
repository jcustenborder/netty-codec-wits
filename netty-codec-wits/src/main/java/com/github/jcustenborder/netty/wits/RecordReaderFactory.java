package com.github.jcustenborder.netty.wits;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RecordReaderFactory {
  Map<Short, RecordReader> lookup = new HashMap<>();

  static class RecordReaderFunction implements Function<Short, RecordReader> {
    @Override
    public RecordReader apply(Short aShort) {

      return null;
    }
  }

  static final RecordReaderFunction READER_FUNCTION = new RecordReaderFunction();

  public RecordReader get(String line) {
    return this.lookup.computeIfAbsent((short)1, READER_FUNCTION);
  }
}

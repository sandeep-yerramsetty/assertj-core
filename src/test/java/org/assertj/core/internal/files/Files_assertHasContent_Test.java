/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.core.internal.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.error.ShouldBeFile.shouldBeFile;
import static org.assertj.core.error.ShouldHaveBinaryContent.shouldHaveBinaryContent;
import static org.assertj.core.error.ShouldHaveContent.shouldHaveContent;
import static org.assertj.core.error.ShouldHaveSameContent.shouldHaveSameContent;
import static org.assertj.core.test.TestData.someInfo;
import static org.assertj.core.util.AssertionsUtil.expectAssertionError;
import static org.assertj.core.util.FailureMessages.actualIsNull;
import static org.assertj.core.util.Files.newFile;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.*;
import org.assertj.core.util.Lists;
import org.assertj.core.util.diff.Delta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for <code>{@link Files#assertHasContent(AssertionInfo, File, String, Charset)}</code>.
 * 
 * @author Olivier Michallat
 * @author Joel Costigliola
 */
class Files_assertHasContent_Test extends FilesBaseTest {

  private static File actual;
  private static String expected;
  private static Charset charset;

  @BeforeAll
  static void setUpOnce() {
    // Does not matter if the values differ, the actual comparison is mocked in this test
    actual = new File("src/test/resources/actual_file.txt");
    expected = "xyz";
    charset = Charset.defaultCharset();
  }

  @Test
  void should_throw_error_if_expected_is_null() {
    assertThatNullPointerException().isThrownBy(() -> files.assertHasContent(someInfo(), actual, null, charset))
                                    .withMessage("The text to compare to should not be null");
  }

  @Test
  void should_fail_if_actual_is_null() {
    assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> files.assertHasContent(someInfo(), null, expected, charset))
                                                   .withMessage(actualIsNull());
  }

  @Test
  void should_fail_if_actual_is_not_file() {
    AssertionInfo info = someInfo();
    File notAFile = new File("xyz");

    Throwable error = catchThrowable(() -> files.assertHasContent(info, notAFile, expected, charset));

    assertThat(error).isInstanceOf(AssertionError.class);
    verify(failures).failure(info, shouldBeFile(notAFile));
  }

  @Test
  void should_pass_if_file_has_text_content() throws IOException {
    String expected = "actual";
    files.assertHasContent(someInfo(), actual, expected, charset);
  }

  @Test
  void should_throw_error_wrapping_caught_IOException() throws IOException {
    IOException cause = new IOException();
    when(diff.diff(actual, expected, charset)).thenThrow(cause);

    assertThatExceptionOfType(UncheckedIOException.class).isThrownBy(() -> files.assertHasContent(someInfo(), actual,
                                                                                                  expected, charset))
                                                         .withCause(cause);
  }

  @Test
  void should_fail_if_file_does_not_have_expected_text_content() throws IOException {
    Diff diff = new Diff();
    List<Delta<String>> diffs = diff.diff(actual, expected, charset);
    AssertionInfo info = someInfo();

    AssertionError error = expectAssertionError(() -> unMockedFiles.assertHasContent(info, actual, expected, charset));

    assertThat(error).isInstanceOf(AssertionError.class);
    then(error).hasMessage(shouldHaveContent(actual, charset, diffs).create(info.description(), info.representation()));
  }
}

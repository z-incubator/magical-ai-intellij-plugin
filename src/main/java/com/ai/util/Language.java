
package com.ai.util;

import java.util.List;

public interface Language {

    List<String> ids();

    String mimeType();

    List<String> fileExtensions();
}

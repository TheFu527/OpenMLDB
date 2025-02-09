# Copyright 2021 4Paradigm
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set(ABSL_URL https://github.com/abseil/abseil-cpp/archive/a50ae369a30f99f79d7559002aba3413dac1bd48.tar.gz)
message(STATUS "build abseil-cpp from ${ABSL_URL}")

ExternalProject_Add(
  absl
  URL ${ABSL_URL}
  URL_HASH SHA256=be2a9d7ea7ee15f9317b57beff37e8ffb67418fb0df64592366b04c8618c2584
  PREFIX ${DEPS_BUILD_DIR}
  DOWNLOAD_DIR ${DEPS_DOWNLOAD_DIR}/absl
  INSTALL_DIR ${DEPS_INSTALL_DIR}
  CONFIGURE_COMMAND ${CMAKE_COMMAND} -H<SOURCE_DIR> -B<BINARY_DIR> -DCMAKE_INSTALL_LIBDIR=lib
    -DCMAKE_INSTALL_PREFIX=${DEPS_INSTALL_DIR} -DCMAKE_BUILD_TYPE=Release
    -DCMAKE_CXX_STANDARD=17 -DABSL_USE_GOOGLETEST_HEAD=OFF -DABSL_RUN_TESTS=OFF
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON ${CMAKE_OPTS}
  BUILD_COMMAND ${CMAKE_COMMAND} --build .
  INSTALL_COMMAND ${CMAKE_COMMAND} --build . --target install)

/*
 * Copyright 2010 Vrije Universiteit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package test.virtual.throughput.mtnio;

class DataSource {

    private int count = 0;
    private boolean done = false;

    synchronized void set(int count) {
        this.count = count;
        notifyAll();
    }

    synchronized void done() {
        done = true;
        notifyAll();
    }

    synchronized boolean waitForStartOrDone() {
        while (!done && count == 0) {
            try {
                wait();
            } catch (Exception e) {
                // ignore
            }
        }

        return done;
    }

    synchronized int getBlock() {

        if (count == 0) {
            return -1;
        }

        count--;

        if (count == 0) {
            notifyAll();
        }

        return count;
    }
}

package org.lendingclub.http.breeze.test;

import java.util.List;
import java.util.Map;

public class TestModel {
    private String message;
    private Map<String, List<String>> map;
    private List<Object> objects;

    public TestModel() {
    }

    public TestModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, List<String>> getMap() {
        return map;
    }

    public void setMap(Map<String, List<String>> map) {
        this.map = map;
    }

    public List<Object> getObjects() {
        return objects;
    }

    public void setObjects(List<Object> objects) {
        this.objects = objects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestModel testModel = (TestModel) o;

        if (message != null ? !message.equals(testModel.message) : testModel.message != null) {
            return false;
        }
        return map != null ? map.equals(testModel.map) : testModel.map == null;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (map != null ? map.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestModel{" + message + ", " + map + "}";
    }
}

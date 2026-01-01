package com.kaua.file.processor.application;

public abstract class UseCase<I, O> {

    public abstract O execute(I input);
}

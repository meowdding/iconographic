package me.owdding.iconographic.utils.extensions

fun <Type> Iterable<Type>.extremesOf(converter: (Type) -> Int): Pair<Int, Int>? {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return null

    var min = converter(iterator.next())
    var max = min

    for (element in this) {
        val value = converter(element)
        if (value < min) min = value
        if (value > max) max = value
    }

    return min to max
}

fun <Type> Iterable<Type>.doubleSum(first: (Type) -> Int, second: (Type) -> Int) = this.doubleIterate(first, second, Integer::sum)

fun <Type, NumberType : Number> Iterable<Type>.doubleIterate(
    first: (Type) -> NumberType,
    second: (Type) -> NumberType,
    combiner: (NumberType, NumberType) -> NumberType
) : Pair<NumberType, NumberType>? {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return null

    val firstElement = iterator.next()
    var firstVar = first(firstElement)
    var secondVar = second(firstElement)

    for (element in this) {
        firstVar = combiner(firstVar, first(element))
        secondVar = combiner(secondVar, second(element))
    }

    return firstVar to secondVar
}

fun <Type> List<Type>.rightPad(size: Int, element: () -> Type): MutableList<Type> {
    if (this !is MutableList<Type>) {
        return this.toMutableList().rightPad(size, element)
    }

    while (this.size < size) {
        this.add(this.lastIndex + 1, element())
    }
    return this
}

local map = {
    test = 2112345678,
    nums = { 1, 2, 3 },
    map = {
        [1] = { v1 = 11, v2 = 100,  },
        [20] = { v1 = 100, v2 = 1000, },
    },
    lists = {
        {v1 = 11, v2 = 22},
        {v1 = 111, v2 = 222},
    },
    string = "123123",
    dmap = {
        [1] = {
            [10] = 11,
        },
    },
    dList = {
        { [10]={
            [1] = {
                [10] = 11,
                },
            },
        },
    },
    number = "aaa",
    lvalue = 9007199254740991,
}
print("aaaa")

return map;
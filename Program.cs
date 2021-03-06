﻿using System;
using System.Collections.Generic;
using System.Text;
using Aspose.Cells;
using System.Drawing;

namespace ybtools
{
    class Program
    {
        class Book
        {
            public int myYellow;
            public int myWhite;
        }
        class Stat
        {
            public Dictionary<String, Book> books = new Dictionary<string, Book>();
            public int otherWhite;
            public int otherYellow;
        };

        static Dictionary<String, String> dic = new Dictionary<string, string>();
        static Dictionary<String, Stat> statMap = new Dictionary<string, Stat>();
        static Dictionary<String, int> skipMap = new Dictionary<string, int>
        {
            { "写了", 1 },
            { "已写了", 1 },
        };
        static void Main(string[] args)
        {
            test1();
        }
        public static void test1()
        {

            var workbook = new Workbook("E:\\tools\\csharp-tools\\excel\\test.xlsx");
            getDic(workbook);
            stat(workbook);

            workbook.Save("E:\\tools\\csharp-tools\\excel\\test1.xlsx");
            Console.ReadKey();
        }
        public static void getDic(Workbook workbook)
        {
            var sh = workbook.Worksheets["index"];
            Console.WriteLine(sh.Name);
            var cell = sh.Cells[0, 0];
            Console.WriteLine(sh.Cells.MaxDataRow);
            for (int i = 0; i <= sh.Cells.MaxDataRow; ++ i)
            {
                var number = sh.Cells[i, 0].Value.ToString();
                var temp = sh.Cells[i, 1];
                String tempValue = "";
                if (dic.TryGetValue(number, out tempValue))
                {
                    Console.WriteLine("发现重复的本号：" + number);
                    break;
                }
                if (temp.IsMerged)
                {
                    dic.Add(number, temp.GetMergedRange().GetCellOrNull(0, 0).Value.ToString());
                }
                else
                {
                    dic.Add(number, temp.Value.ToString());
                }
            }
            Console.WriteLine("count=" + dic.Count);
            Console.WriteLine(dic["E02675"]);
        }

        public static void stat(Workbook workbook)
        {
            var sh = workbook.Worksheets["底表"];
            for (int col = 1; col <= sh.Cells.MaxDataColumn; ++col)
            {
                var numberCell = sh.Cells[0, col];
                if (numberCell == null || numberCell.Value == null) continue;

                var number = numberCell.Value.ToString();
                if (number == null || number.Length <= 0) continue;

                String owner = "";
                if (!dic.TryGetValue(number, out owner))
                {
                    Console.WriteLine("发现错误：本号" + number + "没有配置拥有这");
                    continue;
                }
                
                for (int row = 1; row <= sh.Cells.MaxDataRow; ++row)
                {
                    var workerCell = sh.Cells[row, col];
                    if (workerCell == null || workerCell.Value == null) continue;

                    var worker = workerCell.Value.ToString();
                    if (isSkip(worker)) continue;

                    Stat statInfo;
                    if (!statMap.TryGetValue(worker, out statInfo))
                    {
                        statInfo = new Stat();
                        statMap.Add(worker, statInfo);
                    }
                    var tmp = workerCell.GetStyle();
                    statBook(statInfo, worker.Equals(owner), number, workerCell.GetStyle().ForegroundColor, row, col);
                }
            }

            var result = workbook.Worksheets.Add("result");
            int newRow = 0;
            int newCol = 0;

            result.Cells[newRow, newCol++].Value = "姓名";
            result.Cells[newRow, newCol++].Value = "本号";
            result.Cells[newRow, newCol++].Value = "自己的黄色";
            result.Cells[newRow, newCol++].Value = "自己的白色";
            result.Cells[newRow, newCol++].Value = "其他的黄色";
            result.Cells[newRow, newCol++].Value = "其他的白色";
            ++newRow;
            foreach (var item in statMap)
            {
                var worker = item.Key;
                var statInfo = item.Value;

                newCol = 0;
                if (statInfo.books.Count <= 0)
                {
                    result.Cells[newRow, newCol++].Value = worker;
                    result.Cells[newRow, newCol++].Value = "无";
                    ++newRow;
                    Console.WriteLine(worker + " 无");
                    continue;
                }

                bool isFirst = true;
                foreach (var itBook in statInfo.books)
                {
                    var number = itBook.Key;
                    var book = itBook.Value;


                    result.Cells[newRow, newCol++].Value = worker;
                    result.Cells[newRow, newCol++].Value = number;
                    result.Cells[newRow, newCol++].Value = book.myYellow;
                    result.Cells[newRow, newCol++].Value = book.myWhite;
                    result.Cells[newRow, newCol++].Value = (isFirst ? statInfo.otherYellow : 0);
                    result.Cells[newRow, newCol++].Value = (isFirst ? statInfo.otherWhite : 0);
                    ++newRow;
                    newCol = 0;
                    Console.WriteLine(worker + " " + number + " " + book.myYellow + " " + book.myWhite + " " +
                        (isFirst ? statInfo.otherYellow : 0) + " " + (isFirst ? statInfo.otherWhite : 0));

                    isFirst = false;
                }
            }
        }
        
        static int yellow = Color.Yellow.ToArgb();
        static int white = Color.White.ToArgb();
        static void statBook(Stat statInfo, bool isMy, String number, Color colorIndex, int row, int col)
        {
            Book book = null;
            if (isMy && !statInfo.books.TryGetValue(number, out book))
            {
                book = new Book();
                statInfo.books.Add(number, book);
            }

            int value = colorIndex.ToArgb();
            if (value == yellow)
            {
                if (isMy)
                    book.myYellow++;
                else
                    statInfo.otherYellow++;
            }
            else if (value == white || value == 0)
            {
                if (isMy)
                    book.myWhite++;
                else
                    statInfo.otherWhite++;
            }
            else
            {
                Console.WriteLine("发现错误：" + (row + 1) + "行" + (col + 1) + "列的颜色不是白色和黄色，颜色是" + colorIndex.ToString());
            }
        }

        public static bool isSkip(String worker)
        {
            if (worker == null || worker.Length <= 0) return true;

            int value = 0;
            if (skipMap.TryGetValue(worker, out value)) return true;

            return false;
        }

        public static void test2()
        {
            var workbook = new Workbook("E:\\tools\\csharp-tools\\excel\\test.xlsx");
            var sh = workbook.Worksheets["index"];
            Console.WriteLine(sh.Name);
            var cell = sh.Cells[0, 0].GetMergedRange();
            Console.WriteLine(cell.GetCellOrNull(1, 0).Value);
            Console.ReadKey();
        }
    }
}

000000* SAMPLE03 LEVEL=1 DATE=95.10.01
000100****************************************************************
000200* *
000300* サブシステム名：資材所要量計画 *
000400* *
000500* モジュール名 ：コントロールブレークリスト出力 *
000600* *
000700* モジュールＩＤ：SAMPLE03 *
000800* *
000900* 機能概要 ：明細と合計を印刷する。 *
001000* ： *
001100* *
001200* 作成者 ：山田太郎 *
001300* 作成日 ：1995.10.01 *
001400* 変更履歴 ：1996.08.01 山田　ＸＸＸを追加 *
001500* *
001600****************************************************************
001700 IDENTIFICATION DIVISION.
001800 PROGRAM-ID. SAMPLE03.
001900*
002000 ENVIRONMENT DIVISION.
002100*
002200 INPUT-OUTPUT SECTION.
002300 FILE-CONTROL.
002300 SELECT IN01-FILE ASSIGN IN01.
002400 SELECT OT01-FILE ASSIGN OT01.
002500*
002600 DATA DIVISION.
002700 FILE SECTION.
002800 FD IN01-FILE
002900 BLOCK CONTAINS 0 RECORDS.
003000 01 I1-REC.
003100 COPY AAA001 REPLACING ==()== BY ==I1-==.
003200*
003300 FD OT01-FILE.
003500 01 O1-REC PIC X(300).
003600/
003700 WORKING-STORAGE SECTION.
005100*----ワークエリア----------------------------------------------*
005200 01 WRK-AREA.
004100 03 CAN-AREA.
004200 05 CAN-PGMID PIC X(08) VALUE "SAMPLE03".
005500*
005300　 　03 CNM-AREA. 005400　 　 05 CNM-O1-LINE-MAX PIC S9(04) BINARY VALUE 58.
005500*
005600 03 CTR-AREA.
005700 05 CTR-I1 PIC S9(09) PACKED-DECIMAL.
005800 05 CTR-O1 PIC S9(09) PACKED-DECIMAL.
005900 　 05 CTR-O1-PAGE 　 PIC S9(04) PACKED-DECIMAL.
006000 　 05 CTR-O1-LINE 　 PIC S9(04) PACKED-DECIMAL.
006100*
006200 　03 KEY-AREA.
006300 　 05 KEY-NEW.
006400 　 　07 KEY-NEW-XXX001 PIC X(04).
006500 　 05 KEY-OLD.
006600 　 　07 KEY-OLD-XXX001 PIC X(04).
006700*
006800 　03 MSG-AREA.
006900 　 COPY VDISPMSG.
007000*
007100 　03 SUM-AREA.
007200 　 　05 SUM-FIN-AAA001 　 PIC S9(09) BINARY SYNC.
007300 　 　05 SUM-FIN-BBB001 　 PIC S9(09) BINARY SYNC.
007400 　 　05 SUM-FIN-CCC001 　 PIC S9(09) BINARY SYNC.
007500 　 　05 SUM-TOT-AAA001 　 PIC S9(09) BINARY SYNC.
007600 　 　05 SUM-TOT-BBB001 　 PIC S9(09) BINARY SYNC.
007700 　 　05 SUM-TOT-CCC001 　 PIC S9(09) BINARY SYNC.
007800*
007900*----ヘッダ１--------------------------------------------------*
008000 01 O1-HEAD1.
008100 03 FILLER 　 PIC X(10)
008200 　 VALUE "(SAMPLE03)"
008300 　 POSITION 3.
008400 03 FILLER 　 PIC N(21)
008500 VALUE NC"＊＊＊　金額集計表　＊＊＊"
008600 　 MODE-1 POSITION 46.
008700 03 O1-OP-DATE-YY 　 PIC ZZZ9 POSITION 114.
008800 03 FILLER 　 PIC N(01)
008900 　 VALUE NC"年" MODE-3.
009000 03 O1-OP-DATE-MM 　 PIC Z9.
009100 03 FILLER 　 PIC N(01)
009200 　 VALUE NC"月" MODE-3.
009300 03 O1-OP-DATE-DD 　 PIC Z9.
009400 03 FILLER 　 PIC N(01)
009500 　 VALUE NC"日" MODE-3.
009600 03 O1-PAGE 　 PIC ZZZ9 POSITION 130.
009700 03 FILLER 　 PIC N(01)
009800 　 VALUE NC"頁" MODE-3.
009900*
010000*----ヘッダ２--------------------------------------------------*
010100 01 O1-HEAD2.
010200 　　　03 O1-CODE1　　 PIC ZZZ9 POSITION 5.
010300*
010400*----ヘッダ３--------------------------------------------------*
010500 01 O1-HEAD3.
010600 　　　03 O1-CODE2　　 PIC X(NN) POSITION 5.
010700*
010800*----明細１----------------------------------------------------*
010900 01 O1-MEISAI1.
011000　　　03 O1-MEISAI11 　 　 PIC X(NN) POSITION 2.
011100 　　　03 O1-MEISAI12 　 　 PIC N(NN) MODE-2　POSITION 12.
011200 　　　03 O1-MEISAI13 　 　 PIC X(NN) POSITION 47.
011300*
011400*----明細２----------------------------------------------------*
011500 01 O1-MEISAI2.
011600 　　　03 FILLER 　 　 PIC N(05)
011700 　　　 　 　 VALUE NC"＊営業部計"
011800 　　　 　 　 MODE-2 POSITION 79.
011900 　　　03 O1-SUM-TOT-AAA001 　 　 PIC ZZZ,ZZZ,ZZ9
011900 POSITION 94.
012000 　　　03 O1-SUM-TOT-BBB001 　 　 PIC ZZZ,ZZZ,ZZ9
012010 POSITION 103.
012100 　03 O1-SUM-TOT-CCC001 　 　 PIC ZZZ,ZZZ,ZZ9
012110 POSITION 113.
012200*
012300*----明細３----------------------------------------------------*
012400 01 O1-MEISAI3.
012500　　　03 FILLER　　　　　　　　　　　　PIC N(05)
012600　　　　　　　　　　　　　　　　　　　　　VALUE NC"＊＊総合計"
012700　　　　　　　　　　　　　　　　　　　　　MODE-2 POSITION 79.
012800　　　03 O1-SUM-FIN-AAA001　　　　　　PIC ZZZ,ZZZ,ZZ9
012810　　　　　　　　　　　　　　　　　　　　　POSITION 94.
012900　　　03 O1-SUM-FIN-BBB001　　　　　　PIC ZZZ,ZZZ,ZZ9
012910　　　　　　　　　　　　　　　　　　　　　POSITION 103.
013000　　　03 O1-SUM-FIN-CCC001　　　　　　PIC ZZZ,ZZZ,ZZ9.
013010　　　　　　　　　　　　　　　　　　　　　POSITION 113.
013100*
013200/
013300 PROCEDURE DIVISION.
013400****************************************************************
013500* SAMPLE03 　 　　　　　　　 　　 　　　(0.0)　　 　 　　 *
013600****************************************************************
013700　00-SAMPLE03 SECTION.
013800*
013900　　　PERFORM 10-INIT.
014000　　　PERFORM UNTIL ( KEY-NEW = HIGH-VALUE )
014100　　　　　PERFORM 20-INIT
014200　　　　　PERFORM UNTIL ( KEY-NEW = HIGH-VALUE )
014300　　　　　　　PERFORM 30-INIT
014400　　　　　　　PERFORM UNTIL ( KEY-NEW-XXX001
014500　　　　　　　　　　　　　　　　NOT = KEY-OLD-XXX001 )
014600　　　　　　　　　PERFORM 40-MAIN
014700　　　　　　　END-PERFORM
014800　　　　　　　PERFORM 50-END
014900　　　　　END-PERFORM
015000　　　　　PERFORM 60-END
015100　　　END-PERFORM.
015200　　　PERFORM 70-END.
015300*
015400 00-SAMPLE03-EXIT.
015500　　EXIT PROGRAM.
015600****************************************************************
015700* ＜初期処理＞ 　　 　　　(1.0) 　　　　　　 *
015800* 　 領域の初期クリア、入出力ファイルをオープンする。 　　　 *
015900* 　　 　　　　　　 *
016000****************************************************************
016100 10-INIT SECTION.
016200*
016300*----ファイルＯＰＥＮ------------------------------------------*
016400 OPEN INPUT IN01-FILE.
016500 OPEN OUTPUT OT01-FILE.
016600*
017300 10-INIT-EXIT.
017400 EXIT.
017500****************************************************************
017600* ＜ファイルＲＥＡＤ処理＞　　　 　 　　　(1.1) 　　　　　　 *
017700* 　 標準入力ファイル１をＲＥＡＤする。 　　　 　 　　　 *
017800* 　　 　　　　　　 *
017900****************************************************************
018000 11-READ SECTION.
018100*
018200　　　READ IN01-FILE
018300　　　　AT END
018400　　　　　MOVE HIGH-VALUE TO KEY-NEW
018500　　　　NOT AT END
018600　　　　　COMPUTE CTR-I1 = CTR-I1 + 1
018700　　　END-READ.
018800*
018900 11-READ-EXIT.
019000　　　EXIT.
019100****************************************************************
019200* ＜合計行の初期化＞　　　 　 　 　 　　　(2.0) 　　　 *
019300* 　　　　　　　 *
019400* *
019500****************************************************************
019600 20-INIT SECTION.
019700*
019800*----ページ初期化----------------------------------------------*
019900 MOVE ZERO TO CTR-O1-PAGE.
020000*
020100*----合計行の初期化--------------------------------------------*
020200　　　MOVE ZERO TO SUM-FIN-AAA001
020300　　　　　　　　　　　SUM-FIN-BBB001
020400　　　　　　　　　　　SUM-FIN-CCC001.
020500*
020600 20-INIT-EXIT.
020700 EXIT.
020800****************************************************************
020900* ＜Ｘ計行の初期化＞　 (3.0) *
021000* 　　　　 *
021100* *
021200****************************************************************
021300 30-INIT SECTION.
021400*
021500*----改ページ設定----------------------------------------------*
021600 MOVE CNM-O1-LINE-MAX TO CTR-O1-LINE.
021700*
021800*----Ｘ計行の初期化--------------------------------------------*
021900　　　MOVE　　　　　　　ZERO TO SUM-TOT-AAA001
022000　　　　　　　　　　　　　　　　　SUM-TOT-BBB001
022100　　　　　　　　　　　　　　　　　SUM-TOT-CCC001.
022200*
022300　　　MOVE　　　　　　KEY-NEW TO KEY-OLD.
022400*
022500 30-INIT-EXIT.
022600 EXIT.
022700****************************************************************
022800* ＜主処理＞　 (4.0) *
022900* 　　　　 *
023000* *
023100****************************************************************
023200 40-MAIN SECTION.
023300*
023400*----改ページ判定----------------------------------------------*
023500　　　IF CTR-O1-LINE + 1 > CNM-O1-LINE-MAX
023600　　　　　PERFORM 99-NEWPAGE
023700　　　END-IF.
023800　　　WRITE O1-REC FROM O1-MEISAI1 AFTER 1.
023900　　　COMPUTE CTR-O1-LINE = CTR-O1-LINE + 1.
024000*
024100*----ＺＺＺＺ計加算--------------------------------------------*
024200 COMPUTE SUM-TOT-AAA001 = SUM-TOT-AAA001 + I1-AAA001.
024400 COMPUTE SUM-TOT-BBB001 = SUM-TOT-BBB001 + I1-BBB001.
024600 COMPUTE SUM-TOT-CCC001 = SUM-TOT-CCC001 + I1-CCC001.
024800*
024900 40-MAIN-EXIT.
025000 EXIT.
025100****************************************************************
025200* ＜ＸＸＸＸ計行の印刷＞ (5.0) *
025300* *
025400* *
025500****************************************************************
025600 50-END SECTION.
025700*
025800*----編集------------------------------------------------------*
025900 MOVE SUM-TOT-AAA001 TO O1-SUM-TOT-AAA001.
026000 MOVE SUM-TOT-BBB001 TO O1-SUM-TOT-BBB001.
026100 MOVE SUM-TOT-CCC001 TO O1-SUM-TOT-CCC001.
026200*
026300*----改ページ判定／ＸＸＸＸ計行の印刷--------------------------*
026400 IF CTR-O1-LINE + 1 > CNM-O1-LINE-MAX
026500 PERFORM 99-NEWPAGE
026600 END-IF.
026700 WRITE O1-REC FROM O1-MEISAI2 AFTER 1.
026800 COMPUTE CTR-O1-LINE = CTR-O1-LINE + 1.
026900*
027000*----合計加算--------------------------------------------------*
027100 COMPUTE　SUM-FIN-AAA001　=　SUM-FIN-AAA001　+　SUM-TOT-AAA001.
027300 COMPUTE　SUM-FIN-BBB001　=　SUM-FIN-BBB001　+　SUM-TOT-BBB001.
027500 COMPUTE　SUM-FIN-CCC001　=　SUM-FIN-CCC001　+　SUM-TOT-CCC001.
027700*
027800 50-END-EXIT.
027900 EXIT.
028000****************************************************************
028100* ＜合計行の印刷＞ (6.0) *
028200* *
028300* *
028400****************************************************************
028500 60-END SECTION.
028600*
028700*----編集------------------------------------------------------*
028800 MOVE SUM-FIN-AAA001 TO O1-SUM-FIN-AAA001.
028900 MOVE SUM-FIN-BBB001 TO O1-SUM-FIN-BBB001.
029000 MOVE SUM-FIN-CCC001 TO O1-SUM-FIN-CCC001.
029100*
029200*----改ページ判定／合計行の印刷--------------------------------*
029300 IF CTR-O1-LINE + 1 > CNM-O1-LINE-MAX
029400 PERFORM 99-NEWPAGE
029500 END-IF.
029600 WRITE O1-REC FROM O1-MEISAI3 AFTER 1.
029700*
029800 60-END-EXIT.
029900 EXIT.
030000****************************************************************
030100* ＜終了処理＞　　 　 　　　　　 　 　　　(7.0) 　　　　　　 *
030200* 　 入出力ファイルをＣＬＯＳＥする。　　　　　　 　 　　　 *
030300* 　　 　　　　　　 *
030400****************************************************************
030500 70-END SECTION.
030600*
030700*----ファイルのＣＬＯＳＥ--------------------------------------*
030800 CLOSE IN01-FILE
030900 OT01-FILE.
032000*
032100 70-END-EXIT.
032200 EXIT.
032300****************************************************************
032400* ＜改ページ＞　　 　 　　　　　 　 　　　(9.9) 　　　　　　 *
032500* 　 改ページ処理を行う。 　　　　　　 　 　　　 *
032600* 　　 　　　　　　 *
032700****************************************************************
032800 99-NEWPAGE SECTION.
032900*
033000*----行カウンタ初期化／ページカウンタ設定----------------------*
033100 MOVE ZERO TO CTR-O1-LINE.
033200 COMPUTE CTR-O1-PAGE = CTR-O1-PAGE + 1.
033300 MOVE CTR-O1-PAGE TO O1-PAGE.
033400*
033500*----改ページ処理----------------------------------------------*
033600 MOVE SPACE TO O1-REC.
033700 WRITE O1-REC AFTER PAGE.
033800 WRITE O1-REC FROM O1-HEAD1 AFTER 2.
033900 WRITE O1-REC FROM O1-HEAD2 AFTER 2.
034000 WRITE O1-REC FROM O1-HEAD31 AFTER 2.
034100 WRITE O1-REC FROM O1-HEAD32 AFTER 1.
034200 COMPUTE CTR-O1-LINE = CTR-O1-LINE + 8.
034300*
034400 99-NEWPAGE-EXIT.
034500 EXIT.

000000* SAMPLE01 LEVEL=1 DATE=95.10.01
000100****************************************************************
000200* *
000300* サブシステム名：資材所要量計画 *
000400* *
000500* モジュール名 ：順次ファイルコンバート *
000600* *
000700* モジュールＩＤ：SAMPLE01 *
000800* *
000900* 機能概要 ：1つのファイルをある条件に従って３ファイルに *
001000*　　　　　　　　：出力する。 *
001100* *
001200* 作成者 ：山田太郎 *
001300* 作成日 ：1995.10.01 *
001400* 変更履歴 ：1996.08.01 山田　ＸＸＸを追加 *
001500* *
001600****************************************************************
001700 IDENTIFICATION DIVISION.
001800 PROGRAM-ID. SAMPLE01.
001900*
002000 ENVIRONMENT DIVISION.
002100 INPUT-OUTPUT SECTION.
002200 FILE-CONTROL.
002300　　　SELECT IN01-FILE ASSIGN IN01.
002400　　　SELECT OT01-FILE ASSIGN OT01.
002400　　　SELECT OT02-FILE ASSIGN OT02.
002400　　　SELECT OT03-FILE ASSIGN OT03.
002500/
002600 DATA DIVISION.
002700 FILE SECTION.
002800 FD IN01-FILE
002900　　　　　BLOCK CONTAINS 0 RECORDS.
003000 01 I1-REC.
003100　　　COPY AAA001 REPLACING ==()== BY ==I1-==.
003200*
003300 FD OT01-FILE.
003400 01 O1-REC.
003500　　　 COPY BBB001 REPLACING ==()== BY ==O1-==.
003200*
003300 FD OT02-FILE.
003400 01 O2-REC.
003500　　　 COPY BBB001 REPLACING ==()== BY ==O2-==.
003200*
003300 FD OT03-FILE.
003400 01 O3-REC.
003500　　　 COPY BBB001 REPLACING ==()== BY ==O3-==.
003600/
002400 WORKING-STORAGE SECTION.
002500*
003900*----ワークエリア----------------------------------------------*
004000 01 WRK-AREA.
004100　　　03 CAN-AREA.
004200　　　　　05 CAN-PGMID PIC X(08) VALUE "SAMPLE01".
004300　　　03 CTR-AREA.
004400　　　　　05 CTR-I1 PIC S9(09) PACKED-DECIMAL.
004500　　　　　05 CTR-O1 PIC S9(09) PACKED-DECIMAL.
004500　　　　　05 CTR-O2 PIC S9(09) PACKED-DECIMAL.
004500　　　　　05 CTR-O3 PIC S9(09) PACKED-DECIMAL.
004100　　　03 KEY-AREA.
004200　　　　　05 KEY-NEW PIC X(01).
002700　　　03 MSG-AREA.
002800　　　　　COPY DISPMSG.
004600*
004700*----初期化領域------------------------------------------------*
004800 01 INI-O1-REC.
004900　　　 COPY BBB001 REPLACING ==()== BY ==INI-O1-==.
004600*
004700*----共通領域--------------------------------------------------*
004800 01 W1-REC.
004900　　　 COPY BBB001 REPLACING ==()== BY ==W1-==.
006900/
007000 PROCEDURE DIVISION.
007100****************************************************************
007200* SAMPLE01 (0.0) *
007300****************************************************************
007400 00-SAMPLE01 SECTION.
007100*
007500　　　PERFORM 10-INIT.
007700　　　PERFORM 20-MAIN
008000　　　　　UNTIL KEY-NEW = HIGH-VALUE.
008100　　　PERFORM 30-END.
008200*
008300 00-SAMPLE01-EXIT.
008400　　　EXIT PROGRAM. 
008500/***************************************************************
008600* ＜初期処理＞ (1.0) *
008700* 領域の初期クリア、入出力ファイルをオープンする。 *
008800* *
008900****************************************************************
009000 10-INIT SECTION.
009100*
007200*----出力ファイルの初期化--------------------------------------*
007300 MOVE SPACE TO INI-O1-REC.
007400 INITIALIZE INI-O1-REC.
007900*
008000*----ワークエリアの初期化--------------------------------------*
008100 MOVE LOW-VALUE TO KEY-NEW.
008200 MOVE ZERO TO CTR-AREA.
007500*
007600*----ファイルＯＰＥＮ------------------------------------------*
007700 OPEN INPUT IN01-FILE.
007800 OPEN OUTPUT OT01-FILE
007800　　　　　　　　　 OT02-FILE
007800　　　　　　　　　 OT03-FILE.
007500*
008220*----入力ファイルのＲＥＡＤ------------------------------------*
008230 PERFORM 11-READ.

012100*
012200 10-INIT-EXIT.
012300 EXIT.
012400****************************************************************
012500* ＜ＲＥＡＤ処理＞ (1.1) *
012600* 入力ファイルをＲＥＡＤする。 *
012700* *
012800****************************************************************
012900 11-READ SECTION.
013000*
009400　　　READ IN01-FILE
009500　　　　AT END
009600　　　　　MOVE HIGH-VALUE TO KEY-NEW
009700　　　　NOT AT END
009800　　　　　COMPUTE CTR-I1 = CTR-I1 + 1
009900　　　END-READ.
014000*
014100 11-READ-EXIT.
014200 EXIT.
014300****************************************************************
014400* ＜主処理＞ (2.0) *
014500* レコードを編集してＷＲＩＴＥする。 *
014600* *
014700****************************************************************
014800 20-MAIN SECTION.
014900*
015000*----出力ファイルの初期化--------------------------------------*
015100 MOVE INI-O1-REC TO W1-REC.
015200*
015300*----出力ファイルの編集----------------------------------------*
015400 MOVE I1-AA0001 TO W1-BB0001.
015500 MOVE I1-AA0002 TO W1-BB0002.
015600 MOVE I1-AA0003 TO W1-BB0003.
015700 MOVE I1-AA0004 TO W1-BB0004.
015800 MOVE I1-AA0005 TO W1-BB0005.
015900*
016000*----振り分け＆出力ファイルのＷＲＩＴＥ------------------------*
016100　　　EVALUATE I1-AA0001
016200　　　　　WHEN 1
016300　　　　　WHEN 2
016400　　　　　　　MOVE W1-REC TO O2-REC *1,2の時
016500　　　　　　　PERFORM 22-WRITE
016700　　　　　WHEN 3
016400　　　　　　　MOVE W1-REC TO O3-REC *3の時
016500　　　　　　　PERFORM 23-WRITE
017100　　　　　WHEN OTHER
016400　　　　　　　MOVE W1-REC TO O1-REC *1,2,3以外の時
016500　　　　　　　PERFORM 21-WRITE
017500　　　END-EVALUATE.
017600*
017610*----入力ファイルのＲＥＡＤ------------------------------------*
017620 PERFORM 11-READ.
017630*
017700 20-MAIN-EXIT.
017800 EXIT.
017900****************************************************************
018000* ＜終了処理＞ (3.0) *
018100* 入出力ファイルをＣＬＯＳＥする。 *
018200* *
018300****************************************************************
018400 30-END SECTION.
018500*
014400*----ファイルＣＬＯＳＥ----------------------------------------*
014500 CLOSE IN01-FILE
014600　　　　 OT01-FILE
014600　　　　 OT02-FILE
014600　　　　 OT03-FILE.
020100*
020200* 件数のコンソール表示
020300 MOVE SPACE TO MSG-REC.
020400 MOVE CAN-PGMID TO MSG-PROGRAM-ID.
020500 MOVE 3 TO MSG-TBL-NUM.
020600 MOVE "XXXXX" TO MSG-MESSAGE-B(1).
020700 MOVE CTR-I1 TO MSG-REC-CTR(1).
020800 MOVE "XXXXX" TO MSG-MESSAGE-B(2).
020900 MOVE CTR-O1 TO MSG-REC-CTR(2).
021000 MOVE "XXXXX" TO MSG-MESSAGE-B(3).
021100 MOVE CTR-O2 TO MSG-REC-CTR(3).
021200 CALL "AAAAMSG" USING MSG-AREA.
021300*
021400 30-END-EXIT.
021500 EXIT.
012500/***************************************************************
012600* ＜ＷＲＩＴＥ処理＞ (2.1) *
012700* 出力ファイル１へのＷＲＩＴＥする。 *
012800* *
012900****************************************************************
013000 21-WRITE SECTION.
013100*
013200 WRITE O1-REC.
017000 COMPUTE CTR-O1 = CTR-O1 + 1.
013400*
013500 21-WRITE-EXIT.
013600 EXIT.
012500****************************************************************
012600* ＜ＷＲＩＴＥ処理＞ (2.2) *
012700* 出力ファイル２へのＷＲＩＴＥする。 *
012800* *
012900****************************************************************
013000 22-WRITE SECTION.
013100*
013200 WRITE O2-REC.
017000 COMPUTE CTR-O2 = CTR-O2 + 1.
013400*
013500 22-WRITE-EXIT.
013600 EXIT. 012500****************************************************************
012600* ＜ＷＲＩＴＥ処理＞ (2.3) *
012700* 出力ファイル３へのＷＲＩＴＥする。 *
012800* *
012900****************************************************************
013000 23-WRITE SECTION.
013100*
013200 WRITE O3-REC.
017000 COMPUTE CTR-O3 = CTR-O3 + 1.
013400*
013500 23-WRITE-EXIT.
013600 EXIT.

000000* SAMPLE02 LEVEL=1 DATE=95.10.01
000100****************************************************************
000200* *
000300* サブシステム名：資材所要量計画 *
000400* *
000500* モジュール名 ：順次ファイルのマッチング *
000600* *
000700* モジュールＩＤ：SAMPLE02 *
000800* *
000900* 機能概要 ：２つのファイルをキーに従って *
001000* ：１ファイルにまとめる。 *
001100* *
001200* 作成者 ：山田太郎 *
001300* 作成日 ：1995.10.01 *
001400* 変更履歴 ：1996.08.01 山田　ＸＸＸを追加 *
001500* *
001600****************************************************************
001700 IDENTIFICATION DIVISION.
001800 PROGRAM-ID. SAMPLE02.
001900/
002000 ENVIRONMENT DIVISION.
002100 INPUT-OUTPUT SECTION.
002200 FILE-CONTROL.
002300 SELECT IN01-FILE ASSIGN IN01.
002300 SELECT IN02-FILE ASSIGN IN02.
002400 SELECT OT01-FILE ASSIGN OT01.
002500/
002600 DATA DIVISION.
002700 FILE SECTION.
002800 FD IN01-FILE
002900　　　　BLOCK CONTAINS 0 RECORDS.
003000 01 I1-REC.
003100　　　　 COPY AAA001 REPLACING ==()== BY ==I1-==.
003200*
002800 FD IN02-FILE
002900　　　　 BLOCK CONTAINS 0 RECORDS.
003000 01 I2-REC.
003100　　　　 COPY BBB001 REPLACING ==()== BY ==I2-==.
003200*
003300 FD OT01-FILE.
003400 01 O1-REC.
003500　　　　 COPY CCC001 REPLACING ==()== BY ==O1-==.
003600/
003700 WORKING-STORAGE SECTION.
003800*
003900*----ワークエリア----------------------------------------------*
005400 01 WRK-AREA.
004100　　　03 CAN-AREA.
004200　　　　　05 CAN-PGMID PIC X(08) VALUE "SAMPLE02".
005500　　　03 CTR-AREA.
005600　　　　　05 CTR-I1 PIC S9(09) PACKED-DECIMAL.
005700　　　　　05 CTR-I2 PIC S9(09) PACKED-DECIMAL.
005800　　　　　05 CTR-O1 PIC S9(09) PACKED-DECIMAL.
005900　　　03 KEY-AREA.
006000　　　　　05 KEY-I1.
006100　　　　　　　07 KEY-I1-XX0001 PIC X(01).
006200　　　　　　　07 KEY-I1-YY0001 PIC X(08).
006300　　　　　05 KEY-I2.
006400　　　　　　　07 KEY-I2-XX0001 PIC X(01).
006500　　　　　　　07 KEY-I2-YY0001 PIC X(08).
006600*
004700*----初期化領域------------------------------------------------*
004800 01 INI-O1-REC.
004900　　　 COPY CCC001 REPLACING ==()== BY ==INI-O1-==.
007000/
007100 PROCEDURE DIVISION.
007200****************************************************************
007300* SAMPLE02 (0.0) *
007400****************************************************************
007500 00-SAMPLE02 SECTION.
006600*
007600　　　PERFORM 10-INIT.
007900　　　PERFORM 20-MAIN
007900　　　　　　　　UNTIL ( KEY-I1 = HIGH-VALUE )
008000　　　　　　　　　AND ( KEY-I2 = HIGH-VALUE ).
009600　　　PERFORM 30-END.
009700*
009800 00-SAMPLE02-EXIT.
009900　　　 EXIT PROGRAM.
010000****************************************************************
010100* ＜初期処理＞ (1.0) *
010200* 領域の初期クリア、入出力ファイルをオープンする。 *
010300* *
010400****************************************************************
010500 10-INIT SECTION.
010600*
010700*----出力ファイルの初期化--------------------------------------*
010800 MOVE SPACE TO INI-O1-REC.
010900 INITIALIZE INI-O1-REC.
011000*
008000*----ワークエリアの初期化--------------------------------------*
008100 MOVE LOW-VALUE TO KEY-AREA.
008200 MOVE ZERO TO CTR-AREA.
011500*
007600*----ファイルＯＰＥＮ------------------------------------------*
007700 OPEN INPUT IN01-FILE
007710　　　　　　　 IN02-FILE.
007800 OPEN OUTPUT OT01-FILE.
012110*
012120*----入力ファイル１のＲＥＡＤ----------------------------------*
012130 PERFORM 11-READ.
012130 PERFORM 12-READ.
013000*
013100 10-INIT-EXIT.
013200 EXIT.
013300****************************************************************
013400* ＜ファイルＲＥＡＤ処理＞ (1.1) *
013500* 標準入力ファイル１をＲＥＡＤする。 *
013600* *
013700****************************************************************
013800 11-READ SECTION.
013900*
009400　　　READ IN01-FILE
009500　　　　AT END
014800　　　　　MOVE HIGH-VALUE TO KEY-I1
009700　　　　NOT AT END
009800　　　　　COMPUTE CTR-I1 = CTR-I1 + 1
014500　　　　　MOVE I1-XX0001 TO KEY-I1-XX0001
014600　　　　　MOVE I1-YY0001 TO KEY-I1-YY0001
009900　　　END-READ.
015000*
015100 11-READ-EXIT.
015200 EXIT.
015300****************************************************************
015400* ＜ファイルＲＥＡＤ処理＞ (1.2) *
015500* 標準入力ファイル2 をＲＥＡＤする。 *
015600* *
015700****************************************************************
015800 12-READ SECTION.
015900*
009400　　　READ IN02-FILE
009500　　　　AT END
014800　　　　　MOVE HIGH-VALUE TO KEY-I2
009700　　　　NOT AT END
009800　　　　　COMPUTE CTR-I2 = CTR-I2 + 1
014500　　　　　MOVE I2-XX0001 TO KEY-I2-XX0001
014600　　　　　MOVE I2-YY0001 TO KEY-I2-YY0001
009900　　　END-READ.
017000*
017100 12-READ-EXIT.
017200 EXIT.
017300****************************************************************
017400* ＜マッチング＞ (2.0) *
017500* *
017600* *
017700****************************************************************
017800　20-MAIN SECTION.
017900*
008100　　　EVALUATE TRUE
008200　　　　　WHEN KEY-I1 < KEY-I2
008300　　　　　　　　PERFORM 21-I1-ONLY
008400　　　　　　　　PERFORM 11-READ
008500　　　　　WHEN KEY-I1 > KEY-I2
008600　　　　　　　　PERFORM 22-I2-ONLY
008700　　　　　　　　PERFORM 12-READ
008800　　　　　WHEN KEY-I1 = KEY-I2
008900　　　　　　　　PERFORM UNTIL KEY-I1 NOT = KEY-I2
009000　　　　　　　　　　PERFORM 23-MATCH
009100　　　　　　　　　　PERFORM 12-READ
009200　　　　　　　　　　END-PERFORM
009300　　　　　　　　PERFORM 11-READ
009400　　　END-EVALUATE
018200*
018300　20-MAIN-EXIT.
018400　　　EXIT.
017300****************************************************************
017400* ＜出力ファイル編集１＞ (2.1) *
017500* *
017600* *
017700****************************************************************
017800 21-I1-ONLY SECTION.
017900*IN01-FILEのみの時
018000* ここでは何もしない
018100　　　 CONTINUE.
018200*
018300 21-I1-ONLY-EXIT.
018400 EXIT.
018500****************************************************************
018600* ＜出力ファイル編集２＞ (2.2) *
018700* *
018800* *
018900****************************************************************
019000 22-I2-ONLY SECTION.
021500*IN02-FILEのみの時
021600*----出力ファイルの初期化--------------------------------------*
021700 MOVE INIT-O1-REC TO O1-REC.
021800*
021900*----出力ファイルの編集----------------------------------------*
022000 MOVE I2-AA0001 TO O1-BB0001.
022100 MOVE I2-AA0002 TO O1-BB0002.
022200 MOVE I2-AA0003 TO O1-BB0003.
022300 MOVE I2-AA0004 TO O1-BB0004.
022400 MOVE I2-AA0005 TO O1-BB0005.
022500* 
022600*----出力ファイル１のＷＲＩＴＥ--------------------------------*
012100 PERFORM 221-WRITE.
023000*
020700 22-I2-ONLY-EXIT.
020800 EXIT.
020900****************************************************************
021000* ＜出力ファイル編集３＞ (2.3) *
021100* *
021200* *
021300****************************************************************
021400 23-MATCH SECTION.
021500*
021600*----出力ファイルの初期化--------------------------------------*
021700 MOVE INIT-O1-REC TO O1-REC.
021800*
021900*----出力ファイルの編集----------------------------------------*
022000 MOVE I2-AA0001 TO O1-BB0001.
022100 MOVE I2-AA0002 TO O1-BB0002.
022200 MOVE I2-AA0003 TO O1-BB0003.
022300 MOVE I2-AA0004 TO O1-BB0004.
022400 MOVE I2-AA0005 TO O1-BB0005.
022500* 
022600*----出力ファイル１のＷＲＩＴＥ--------------------------------*
012100 PERFORM 221-WRITE.
023000*
023100 23-MATCH-EXIT.
023200 EXIT.
023300****************************************************************
023400* ＜終了処理＞ (3.0) *
023500* 入出力ファイルをＣＬＯＳＥする。 *
023600* *
023700****************************************************************
023800 30-END SECTION.
023900*
014400*----ファイルＣＬＯＳＥ----------------------------------------*
014500 CLOSE IN01-FILE
014600　　　　　 IN02-FILE
014600　　　　　 OT01-FILE.
025100*
026400 30-END-EXIT.
026500 EXIT.
028600****************************************************************
028700* ＜書出処理＞ (2.2.1) *
028800* 順編成ファイル出力。 *
028900* *
029000****************************************************************
029100 221-WRITE SECTION.
029300* 
029310 WRITE O1-REC.
013300 COMPUTE CTR-O1 = CTR-O1 + 1. 
029300*
029400 221-WRITE-EXIT.
029500 EXIT.

package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.AbstractTest

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2023
 */
class DigestUtilsTest extends AbstractTest {

    def "Check SRI encoding"() {

        when: "encoding sri"
        String res = DigestUtils.buildSri(fileFromClasspath('bootstrap.min.css', '/sri/bootstrap.min.css'), 'SHA-384')
        
        then: "correct"
        res == 'sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65'
    }

    def "Check SRI decoding"() {

        when: "decode sti token"
        DigestUtils.SriToken token = DigestUtils.parseSri('sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65')
        println token
        
        then: "parse ok"
        token.alg == 'SHA-384'
        token.tokenString == '��\u0000�PJB\u0018 �<G��h\n�� ��<�5�Ek��P�/\u0006I\u0011Ҷ)�j�_BPn�'
        
        and: "same token compute token manually"
        token.token == DigestUtils.hash(fileFromClasspath('bootstrap.min.css', '/sri/bootstrap.min.css'), 'SHA-384')
    }

}

package graphql.introspection;

import graphql.language.Comment;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.SourceLocation;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static graphql.Assert.assertShouldNeverHappen;
import static graphql.Assert.assertTrue;

public class IntrospectionResultToSchema {


    public String introscpectionResultToSchema(Map<String, Object> introspectionResult) {
        return null;
    }

    private List<Comment> toComment(String description) {
        if (description == null) return Collections.emptyList();
        Comment comment = new Comment(description, new SourceLocation(1, 1));
        return Arrays.asList(comment);
    }

    @SuppressWarnings("unchecked")
    public EnumTypeDefinition createEnum(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("ENUM"), "wrong input");

        EnumTypeDefinition enumTypeDefinition = new EnumTypeDefinition((String) input.get("name"));
        enumTypeDefinition.setComments(toComment((String) input.get("description")));

        List<Map<String, Object>> enumValues = (List<Map<String, Object>>) input.get("enumValues");

        for (Map<String, Object> enumValue : enumValues) {

            EnumValueDefinition enumValueDefinition = new EnumValueDefinition((String) enumValue.get("name"));
            enumValueDefinition.setComments(toComment((String) enumValue.get("description")));
            enumTypeDefinition.getEnumValueDefinitions().add(enumValueDefinition);
        }

        return enumTypeDefinition;
    }

    @SuppressWarnings("unchecked")
    public InterfaceTypeDefinition createInterface(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("INTERFACE"), "wrong input");

        InterfaceTypeDefinition interfaceTypeDefinition = new InterfaceTypeDefinition((String) input.get("name"));
        interfaceTypeDefinition.setComments(toComment((String) input.get("description")));
        List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("fields");
        interfaceTypeDefinition.getFieldDefinitions().addAll(createFields(fields));

        return interfaceTypeDefinition;

    }


    @SuppressWarnings("unchecked")
    public ObjectTypeDefinition createObject(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("OBJECT"), "wrong input");

        ObjectTypeDefinition objectTypeDefinition = new ObjectTypeDefinition((String) input.get("name"));
        objectTypeDefinition.setComments(toComment((String) input.get("description")));
        List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("fields");

        objectTypeDefinition.getFieldDefinitions().addAll(createFields(fields));

        return objectTypeDefinition;
    }

    private List<FieldDefinition> createFields(List<Map<String, Object>> fields) {
        List<FieldDefinition> result = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            FieldDefinition fieldDefinition = new FieldDefinition((String) field.get("name"));
            fieldDefinition.setComments(toComment((String) field.get("description")));
            fieldDefinition.setType(createType((Map<String, Object>) field.get("type")));

            List<Map<String, Object>> args = (List<Map<String, Object>>) field.get("args");

            for (Map<String, Object> arg : args) {
                Type argType = createType((Map<String, Object>) arg.get("type"));
                InputValueDefinition inputValueDefinition = new InputValueDefinition((String) arg.get("name"), argType);

                if (arg.get("defaultValue") != null) {
                    StringValue defaultValue = new StringValue((String) arg.get("defaultValue"));
                    inputValueDefinition.setDefaultValue(defaultValue);
                }
                fieldDefinition.getInputValueDefinitions().add(inputValueDefinition);
            }
            result.add(fieldDefinition);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    Type createType(Map<String, Object> type) {
        String kind = (String) type.get("kind");
        switch (kind) {
            case "INTERFACE":
            case "OBJECT":
            case "UNION":
            case "ENUM":
                return new TypeName((String) type.get("name"));
            case "SCALAR":
                return new TypeName((String) type.get("name"));
            case "NON_NULL":
                return new NonNullType(createType((Map<String, Object>) type.get("ofType")));
            case "LIST":
                return new ListType(createType((Map<String, Object>) type.get("ofType")));
            default:
                return assertShouldNeverHappen("Unknown kind " + kind);
        }
    }

}